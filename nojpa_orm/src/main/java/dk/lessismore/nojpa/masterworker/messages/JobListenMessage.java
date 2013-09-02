package dk.lessismore.nojpa.masterworker.messages;


public class JobListenMessage extends JobRelatedMessage {

    public JobListenMessage(String jobID) {
        super(jobID);
    }

    public JobListenMessage() {
    }
}
