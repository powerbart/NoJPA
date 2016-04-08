package dk.lessismore.nojpa.masterworker.messages;

public class KillMessage extends JobRelatedMessage {

    public KillMessage(String jobID) {
        super(jobID);
    }

    public KillMessage() {
    }
}
