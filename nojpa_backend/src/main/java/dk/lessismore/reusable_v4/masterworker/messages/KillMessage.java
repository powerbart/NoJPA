package dk.lessismore.reusable_v4.masterworker.messages;

public class KillMessage extends JobRelatedMessage {

    public KillMessage(String jobID) {
        super(jobID);
    }

    public KillMessage() {
    }
}
