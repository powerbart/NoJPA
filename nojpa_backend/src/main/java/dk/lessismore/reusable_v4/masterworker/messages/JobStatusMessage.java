package dk.lessismore.reusable_v4.masterworker.messages;

import dk.lessismore.reusable_v4.masterworker.JobStatus;


public class JobStatusMessage extends JobRelatedMessage {

    private JobStatus status;

    public JobStatusMessage() {
    }

    public JobStatusMessage(String jobID, JobStatus status){
        super(jobID);
        this.status = status;
    }


    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
