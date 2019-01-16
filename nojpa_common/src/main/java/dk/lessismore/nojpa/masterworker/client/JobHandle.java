package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.concurrency.WaitForValue;
import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.masterworker.exceptions.JobHandleClosedException;
import dk.lessismore.nojpa.masterworker.exceptions.TimeoutException;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.messages.NewRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;

public class JobHandle<O> {

    private static final Logger log = LoggerFactory.getLogger(JobHandle.class);

    private final JobHandleToMasterProtocol<O> jm;
    private final Class implementationClass;
    private final Object jobData;
    private final String jobID;
    private boolean closed = false;

    // Default job values
    private JobStatus jobStatus = JobStatus.QUEUED;
    private double jobProgress = 0;
    private final WaitForValue<Pair<O, RuntimeException>> result = new WaitForValue<Pair<O, RuntimeException>>();
    private final HashMap<String, WaitForValue<Pair<Object, RuntimeException>>> runMethodRemoteResultMap = new HashMap<String, WaitForValue<Pair<Object, RuntimeException>>>();


    /**
     * Queue job in the master-worker setup and create a handle to obtain
     * status information and the result of the job.
     * @param jm helper object implementing the network logic.
     * @param executorClass Class carrying the algorithem of the job.
     * @param jobData Input for the executor class's run method.
     */
    public JobHandle(JobHandleToMasterProtocol<O> jm, Class<? extends Executor> executorClass, Object jobData, long deadline) {
        String tmpObjectID = GuidFactory.getInstance().makeGuid();
        try{
            Method getIDMethod = jobData.getClass().getMethod("getID");
            Object id =  getIDMethod.invoke(jobData, null);
            if(id != null && (("" + id).length() > 2)){
                tmpObjectID = "" + id;
            }
        } catch (Exception e){}
        this.jobID = tmpObjectID;
        this.jm = jm;
        this.implementationClass = executorClass;
        this.jobData = jobData;
        log.debug("Constructor and sending jobID("+ jobID +")");
        jm.sendRunJobRequest(jobID, executorClass, jobData, new Listener(), deadline);
    }

    public JobHandle(JobHandleToMasterProtocol<O> jm, Class<? extends RemoteBeanInterface> executorClass) {
        this.jobID = GuidFactory.getInstance().makeGuid();
        this.jm = jm;
        this.implementationClass = executorClass;
        this.jobData = new NewRemoteBeanMessage();
        //String objectID, Class<? extends Executor> executorClass, Object jobData
        jm.sendRunJobRequest(jobID, executorClass, jobData, new Listener(), -1);
    }


    public Class<? extends Executor> getImplementationClass() {
        return implementationClass;
    }

    public Object getJobData() {
        return jobData;
    }

    public String getJobID() {
        return jobID;
    }

    /**
     * @return The last known job status from master.
     */
    public JobStatus getStatus() {
        if(jobStatus == JobStatus.DONE){
            return jobStatus;
        }
        if (closed) throw new JobHandleClosedException();
        return jobStatus;
    }

    /**
     * @return The last known job progress from master.
     */
    public double getProgress() {
        if(jobStatus == JobStatus.DONE){
            return jobProgress;
        }
        if (closed) throw new JobHandleClosedException();
        return jobProgress;
    }

    /**
     * Case Status of job
     *   QUEUE: Dequeue job.
     *   RUNNING: Try to stop execution on worker by setting the stopNicely flag on the executer object.
     *   DONE: Descard result.
     * Close this handle.
     */
    public void stopNicely() {
        if (closed) throw new JobHandleClosedException();
        jm.stopNicely(jobID);
        close();
    }

    /**
     * Case Status of job
     *   QUEUE: Dequeue job.
     *   RUNNING: Terminate execution on worker.
     *   DONE: Descard result.
     * Close this handle.
     */
    public void kill() {
        if (closed) throw new JobHandleClosedException();
        jm.kill(jobID);
        close();
    }

    public void closeProtocol() {
        jm.close();
        close();
    }

    public Object runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage)  throws Throwable {
        try{
            runMethodRemoteBeanMessage.setJobID( jobID );
            WaitForValue<Pair<Object, RuntimeException>> waitForValue = new WaitForValue<Pair<Object, RuntimeException>>();
            runMethodRemoteResultMap.put(runMethodRemoteBeanMessage.getMethodID(), waitForValue);
            jm.runMethodRemote(runMethodRemoteBeanMessage, waitForValue);
            Pair<Object, RuntimeException> pair = waitForValue.getValue();
            jm.setWaitForValueToNull();
            Object value = pair.getFirst();
            RuntimeException exception = pair.getSecond();
            if (exception != null) {
                throw exception;
            } else {
                if(value == null) return null;
                if(value instanceof RunMethodRemoteResultMessage){
                    RunMethodRemoteResultMessage msg = (RunMethodRemoteResultMessage) value;
                    if(msg.hasException()){
                        throw msg.getException(jm.serializer);
                    } else {
                        Object o = msg.getResult(jm.serializer);
                        return o;
                    }
                }
                return value;
            }
        } finally {
            runMethodRemoteResultMap.remove(runMethodRemoteBeanMessage.getMethodID());
        }

    }



    /**
     * @return The result of the job executed on the master-worker system.
     * It will block until the job is done or an exception occurs.
     * Exceptions may come from the execution of the algorithm, or from network communication problems.
     * Checked Exception (Errors) thrown from the executed job are wrapped in a WrappedErrorException.
     */
    public O getResult() {
        O value = null;
        RuntimeException exception = null;
        try {
            if (jobStatus != JobStatus.DONE && closed) {
                throw new JobHandleClosedException();
            }
            Pair<O, RuntimeException> pair = result.getValue();
            value = pair.getFirst();
            exception = pair.getSecond();
            if (exception != null) {
                throw exception;
            } else {
                return value;
            }
        } finally {
            log.debug("getResult() return: value("+ value +"), exception("+ exception +")");
            MasterService.putBackInPool(jm);
        }
    }

    /**
     * Add callback listener for job status changes and the final job result.
     * @param listener Callback
     */
    public void addJobListener(JobListener<O> listener) {
        if (closed) throw new JobHandleClosedException();
        jm.setJobListener(listener);
    }

    /**
     * Remove callback listener.
     * @param listener Callback
     */
    public void removeJobListener(JobListener<O> listener) {
        if (closed) throw new JobHandleClosedException();
        jm.removeJobListener(listener);
    }

    /**
     * Unregister all listeners created with this handle and close connection to master.
     * Subsequent calls to getStatus(), getProgress(), stopNicely(), kill(),
     * close(), getResult() and setJobListener will raise JobHandleClosedException.
     */
    public void close() {
//        if (closed) throw new JobHandleClosedException();
        closed = true;
        //We should NOT call close on the jm.... Since we are reusing the connection ...
        //jm.close();
        if(!result.hasValue()){
            log.info("SETTING RESULT TO NULL.....!!!!");
            result.setValue(new Pair<O, RuntimeException>(null, new JobHandleClosedException()));
        }
    }

    public void timeout() {
        jobStatus = JobStatus.DONE;
        if(!result.hasValue()){
            log.info("SETTING RESULT TO NULL -TIMEOUT.....!!!!");
            result.setValue(new Pair<O, RuntimeException>(null, new TimeoutException()));
        } else {
            result.resignal();
        }
        stopNicely();
    }



    private class Listener implements JobListener<O> {

        @Override
        public String getJobID() {
            return jobID;
        }

        @Override
        public String toString() {
            return "JobHandleListener.jobID("+ jobID +")";
        }

        public void onStatus(JobStatus status) {
            jobStatus = status;
        }

        public void onProgress(double progress) {
            jobProgress = progress;
        }

        public void onResult(O value) {
            log.debug("Setting result for jobID[" + jobID + "] result ["+ value +"]" );
            jobProgress = 1;
            jobStatus = JobStatus.DONE;
            result.setValue(new Pair<O, RuntimeException>(value, null));

        }

        public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
            WaitForValue<Pair<Object, RuntimeException>> waitForValue = runMethodRemoteResultMap.get(runMethodRemoteResultMessage.getMethodID());
            if(waitForValue != null){
                log.debug("Setting result on waitForValue : result("+ result +")");
                waitForValue.setValue(new Pair<Object, RuntimeException>(runMethodRemoteResultMessage.getResult(jm.serializer), runMethodRemoteResultMessage.getException(jm.serializer)));
            } else {
                log.error("Got a RunMethodRemoteResultMessage ("+ runMethodRemoteResultMessage +") - but no client to give the result to .... :( ");
            }
        }


        public void onException(RuntimeException e) {
            log.debug("Setting exception for jobID[" + jobID + "]");
            jobProgress = 1;
            jobStatus = JobStatus.DONE;
            result.setValue(new Pair<O, RuntimeException>(null, e));
        }
    }

}
