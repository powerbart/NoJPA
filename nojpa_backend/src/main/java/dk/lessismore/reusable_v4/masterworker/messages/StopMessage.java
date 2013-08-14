package dk.lessismore.reusable_v4.masterworker.messages;

public class StopMessage extends JobRelatedMessage {

    public StopMessage(String jobID) {
        super(jobID);
    }

    public StopMessage() {
    }
}
