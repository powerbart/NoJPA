package dk.lessismore.nojpa.masterworker.bean.client;

import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
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

    private static Serializer defaultSerializer() {
        return new XmlSerializer();
    }

    public static <I, O> JobHandle<O> runRemoteBean(Class<? extends RemoteBeanInterface> sourceClass) {
        JobHandleToMasterProtocol<O> jm = new JobHandleToMasterProtocol<O>(defaultSerializer());
        return new JobHandle<O>(jm, sourceClass);
    }
}
