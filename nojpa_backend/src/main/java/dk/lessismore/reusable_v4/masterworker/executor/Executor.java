package dk.lessismore.reusable_v4.masterworker.executor;

public abstract class Executor<I, O> {

    private double progress = 0; // Between 0 and 1
    private boolean stopNicely = false;

    public abstract O run(I input);

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }

    public void stopNicely(){
        stopNicely = true;
    }

    public boolean isStoppedNicely() {
        return stopNicely;
    }

}
