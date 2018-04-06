package dk.lessismore.nojpa.masterworker.messages;

import dk.lessismore.nojpa.masterworker.JobStatus;


public class JobStatusMessage extends JobRelatedMessage {

    private JobStatus status;
    private String workerID;

    public JobStatusMessage() {
    }

    public JobStatusMessage(String jobID, JobStatus status, String workerID){
        super(jobID);
        this.status = status;
        this.workerID = workerID;
    }

    public String getWorkerID() {
        return workerID;
    }

    public void setWorkerID(String workerID) {
        this.workerID = workerID;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
