package dk.lessismore.nojpa.masterworker.messages;

public class StopMessage extends JobRelatedMessage {

    public StopMessage(String jobID) {
        super(jobID);
    }

    public StopMessage() {
    }
}
