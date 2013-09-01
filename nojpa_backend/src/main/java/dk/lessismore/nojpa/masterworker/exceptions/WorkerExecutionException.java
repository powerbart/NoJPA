package dk.lessismore.nojpa.masterworker.exceptions;

public class WorkerExecutionException extends MasterWorkerException {
    public WorkerExecutionException() {
    }

    public WorkerExecutionException(String s) {
        super(s);
    }

    public WorkerExecutionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public WorkerExecutionException(Throwable throwable) {
        super(throwable);
    }
}