package dk.lessismore.reusable_v4.masterworker.exceptions;

public abstract class MasterWorkerException extends RuntimeException {
    public MasterWorkerException() {
    }

    public MasterWorkerException(String s) {
        super(s);
    }

    public MasterWorkerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MasterWorkerException(Throwable throwable) {
        super(throwable);
    }
}
