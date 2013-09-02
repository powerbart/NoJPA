package dk.lessismore.nojpa.masterworker.messages;

public class JobRelatedMessage {
    private String jobID;

    public JobRelatedMessage() {
    }

    public JobRelatedMessage(String jobID) {
        this.jobID = jobID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }
}
