package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.masterworker.observer.AbstractObserver;
import dk.lessismore.nojpa.serialization.Serializer;
import dk.lessismore.nojpa.serialization.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterService {

    private final static Logger log = LoggerFactory.getLogger(MasterService.class);

    public static <I,O> JobHandle<O> runJob(Class<? extends Executor<I,O>> implementationClass, I jobData, int maxSecondsBeforeFailure) {
        JobHandle<O> jobHandle = runJob(implementationClass, jobData, defaultSerializer());
        final Object blocker = new Object();
        final Object resultBocker = new Object();

        final String DEBUG_ID = jobHandle.getJobID().substring(jobHandle.getJobID().length() - 6);
        Thread blockerThread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; jobHandle.getStatus() != JobStatus.DONE && i < maxSecondsBeforeFailure; i++){
                    try {
                        synchronized (blocker) {
                            if(jobHandle.getStatus() != JobStatus.DONE){
                                blocker.wait(1000);
                            }
                        }
                    } catch (InterruptedException e) {
                        log.debug(" Got-InterruptedException for ("+ jobHandle.getJobID() +"):" +e);
                    }
                    if(jobHandle.getStatus() != JobStatus.DONE){
                        if(i > (maxSecondsBeforeFailure / 2)) {
                            log.debug(" Waiting[" + i + "/" + maxSecondsBeforeFailure + "] for JobHandle(" + jobHandle.getJobID() + ") status(" + jobHandle.getStatus() + ") progress(" + jobHandle.getProgress() + ") TYPE[" + implementationClass.getSimpleName() + "]");
                        }
                    }
                }
                if(jobHandle.getStatus() != JobStatus.DONE){
                    log.warn(" MW-NO-RESULT Waiting: We didn't get any result from JobHandle("+ jobHandle.getJobID() +") status(" + jobHandle.getStatus() +") progress(" + jobHandle.getProgress() +")");
                }
                if(jobHandle.getStatus() != JobStatus.DONE) {
                    log.warn("timeout-TIMEOUT for job("+ jobHandle.getJobID()  +")...!!!!!!!! =!=!=!=!=!=!=!=!=!");
                    log.warn("timeout-TIMEOUT for job("+ jobHandle.getJobID()  +")...!!!!!!!! =!=!=!=!=!=!=!=!=!");
                    log.warn("timeout-TIMEOUT for job("+ jobHandle.getJobID()  +")...!!!!!!!! =!=!=!=!=!=!=!=!=!");
                    jobHandle.timeout();
                }
            }
        };


        blockerThread.setName(Thread.currentThread().getName() + "-" + blockerThread.getName() + "-" + DEBUG_ID);
        blockerThread.start();
        return jobHandle;
    }

    public static <I,O> JobHandle<O> runJob(Class<? extends Executor<I,O>> implementationClass, I jobData) {
        return runJob(implementationClass,  jobData, defaultSerializer());
    }

    public static <I,O> JobHandle<O> runJob(Class<? extends Executor<I,O>> implementationClass, I jobData, Serializer serializer) {
        JobHandleToMasterProtocol<O> jm = new JobHandleToMasterProtocol<O>(serializer);
        return new JobHandle<O>(jm, implementationClass, jobData);
    }

    public static <I, O> JobHandle<O> restoreJobHandle(Class<? extends Executor<I,O>>implementationClass, String jobID) {
        return restoreJobHandle(implementationClass, jobID, defaultSerializer());
    }

    public static <I, O> JobHandle<O> restoreJobHandle(Class<? extends Executor<I,O>>implementationClass, String jobID, Serializer serializer) {
        JobHandleToMasterProtocol<O> jm = new JobHandleToMasterProtocol<O>(serializer);
        return new JobHandle<O>(jm, jobID);
    }
    
    public static  <I,O> BatchJobHandle<O> runBatchJob(
            Class<? extends Executor<I,O>> implementationClass, I[] jobDatas, boolean stopOnFirstError) {
        return new BatchJobHandle<O>();
    }

    public static void restartAllWorkers() {
        JobHandleToMasterProtocol jm = new JobHandleToMasterProtocol(defaultSerializer());
        jm.restartAllWorkers();
    }


    public static UpdateMessage getStatus(){
        CurrentStatus c = new CurrentStatus();
        while(c.getUpdateMessage() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        c.stop();
        return c.getUpdateMessage();
    }

    private static class CurrentStatus extends AbstractObserver {

        UpdateMessage updateMessage = null;

        @Override
        protected void update(UpdateMessage updateMessage) {
            this.updateMessage = updateMessage;
        }

        public UpdateMessage getUpdateMessage() {
            return updateMessage;
        }

        public void setUpdateMessage(UpdateMessage updateMessage) {
            this.updateMessage = updateMessage;
        }
    }




    private static Serializer defaultSerializer() {
        return new XmlSerializer();
    }
}
