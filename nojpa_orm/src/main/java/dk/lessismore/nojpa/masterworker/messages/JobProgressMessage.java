package dk.lessismore.nojpa.masterworker.messages;

public class JobProgressMessage extends JobRelatedMessage {

    private double progress;

    public JobProgressMessage(String jobID, double progress) {
        super(jobID);
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

}
