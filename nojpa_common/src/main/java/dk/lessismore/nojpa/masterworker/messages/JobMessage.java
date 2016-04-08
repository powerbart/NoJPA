package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.executor.Executor;


public class JobMessage extends JobRelatedMessage {

    private String executorClassName;
    private String serializedJobData;

    public JobMessage() {
    }

    public JobMessage(String jobID, Class<? extends Executor> executorClass, String serializedJobData){
        super(jobID);
        this.executorClassName = executorClass.getName();
        this.serializedJobData = serializedJobData;
    }


    public String getExecutorClassName() {
        return executorClassName;
    }

    public void setExecutorClassName(String executorClassName) {
        this.executorClassName = executorClassName;
    }

    public String getSerializedJobData() {
        return serializedJobData;
    }

    public void setSerializedJobData(String serializedJobData) {
        this.serializedJobData = serializedJobData;
    }
}
