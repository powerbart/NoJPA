package dk.lessismore.nojpa.masterworker.bean.client;

import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.serialization.Serializer;
import dk.lessismore.nojpa.serialization.XmlSerializer;

public class MasterService {

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

    private static Serializer defaultSerializer() {
        return new XmlSerializer();
    }
}
