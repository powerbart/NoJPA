package dk.lessismore.nojpa.masterworker.executor;

public abstract class Executor<I, O> {

    private double progress = 0; // Between 0 and 1

    public abstract O run(I input);

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }

    public void cancelCurrentJob(){ }

    public boolean isExecutingJob() {
        return false;
    }

    public boolean isRemoteBeanClosing() {
        return false;
    }

}
