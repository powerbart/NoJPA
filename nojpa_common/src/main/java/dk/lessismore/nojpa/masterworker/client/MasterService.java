package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.masterworker.observer.AbstractObserver;
import dk.lessismore.nojpa.pool.ResourcePool;
import dk.lessismore.nojpa.pool.factories.ResourceFactory;
import dk.lessismore.nojpa.serialization.Serializer;
import dk.lessismore.nojpa.serialization.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


//This class has the purpose the be the "interface" against the client

public class MasterService {

    private final static Logger log = LoggerFactory.getLogger(MasterService.class);

    private static ResourcePool pool = null;

    public static <I, O> JobHandle<O> runRemoteBean(Class<? extends RemoteBeanInterface> sourceClass) {
        JobHandleToMasterProtocol<O> jm = new JobHandleToMasterProtocol<O>(defaultSerializer());
        return new JobHandle<O>(jm, sourceClass);
    }

    public static <I,O> JobHandle<O> runJob(Class<? extends Executor<I,O>> implementationClass, I jobData, int maxSecondsBeforeFailure) {
        JobHandleToMasterProtocol<O> jm = getNewJobHandleToMasterProtocol();
        JobHandle<O> jobHandle = new JobHandle<O>(jm, implementationClass, jobData, System.currentTimeMillis() + maxSecondsBeforeFailure * 1000);
        final Object blocker = new Object();

        //TODO: In this case, we have one thread living as long as the request is active + 1 second.
        //TODO: We should make one shared thread that is checking all jobHandles and timing it out, when needed.
        MDC.put("jobID", jobHandle.getJobID());
        MDC.put("workerID", jm.getClientLink().getLinkID());

        Thread blockerThread = new Thread() {
            @Override
            public void run() {
                for(int i = 0; jobHandle.getStatus() != JobStatus.DONE && i < maxSecondsBeforeFailure +10; i++){
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
                        if(i > (maxSecondsBeforeFailure / 2) && (i % 10 == 0)) {
                            log.warn(" Waiting[" + i + "/" + maxSecondsBeforeFailure + "] for JobHandle(" + jobHandle.getJobID() + ") status(" + jobHandle.getStatus() + ") progress(" + jobHandle.getProgress() + ") TYPE[" + implementationClass.getSimpleName() + "]");
                        }
                    }
                }
                if(jobHandle.getStatus() != JobStatus.DONE){
                    log.warn(" MW-NO-RESULT Waiting: We didn't get any result from JobHandle("+ jobHandle.getJobID() +") status(" + jobHandle.getStatus() +") progress(" + jobHandle.getProgress() +")");
                    log.warn("timeout-TIMEOUT for job("+ jobHandle.getJobID()  +")...!!!!!!!! =!=!=!=!=!=!=!=!=! " + jobHandle.getImplementationClass().getSimpleName());
                    jobHandle.timeout();
                }
            }
        };

        if(maxSecondsBeforeFailure > 0) {
            blockerThread.setName("TimeoutThread-"+ jobHandle.getJobID().substring(jobHandle.getJobID().length() - 6));
            blockerThread.start();
        }
        return jobHandle;
    }


    public static void closeAllConnections(){
        //TODO: Should call close on all JobHandleToMasterProtocol objects, and the MasterService should be uncallable after.
    }

    public static <O> void putBackInPool(JobHandleToMasterProtocol<O> jm) {
        log.debug("putBackInPool for JobHandleToMasterProtocol:");
        jm.removeAllJobListeners();
        pool.putBackInPool(jm);
    }


    //TODO: Serializer should not be a parameter, we will always use the XmlSerializer....!
    public synchronized static <O> ResourcePool getJobHandleToMasterProtocolPool(){
        if(pool == null){
            pool = new ResourcePool(new ResourceFactory() {
                @Override
                public Object makeResource() {
                    return new JobHandleToMasterProtocol<O>(defaultSerializer());
                }

                @Override
                public void closeResource(Object resource) {
                    ((JobHandleToMasterProtocol) resource).close();
                }

                @Override
                public String debugName() {
                    return "JobHandleToMasterProtocol";
                }

                @Override
                public int maxWaitSecBeforeCreatingNewResource() {
                    return 0;
                }
            }, 1);
        }
        return pool;
    }

    public static JobHandleToMasterProtocol getNewJobHandleToMasterProtocol() {
        ResourcePool p = getJobHandleToMasterProtocolPool();
        log.debug("getNewJobHandleToMasterProtocol: p.getNrOfResources("+ p.getNrOfResources() +")");
        if (p.getNrOfResources() < 1) {
            p.addNew();
        }
        JobHandleToMasterProtocol protocol = (JobHandleToMasterProtocol) p.getFromPool();
        if (!protocol.getClientLink().isWorking()) {
            return (JobHandleToMasterProtocol) p.addNew();
        }
        return protocol;
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
