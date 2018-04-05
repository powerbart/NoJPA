package dk.lessismore.nojpa.masterworker.messages;

public class CancelJobMessage extends JobRelatedMessage {

    public CancelJobMessage(String jobID) {
        super(jobID);
    }

    public CancelJobMessage() {
    }
}
