package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.executor.Executor;


public class JobMessage extends JobRelatedMessage {

    private String executorClassName;
    private String serializedJobData;
    private long dealline;

    public JobMessage() {
    }

    public JobMessage(String jobID, Class executorClass, String serializedJobData, long dealline){
        super(jobID);
        this.executorClassName = executorClass.getName();
        this.serializedJobData = serializedJobData;
        this.dealline = dealline;
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

    public long getDealline() {
        return dealline;
    }
}
