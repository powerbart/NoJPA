package dk.lessismore.reusable_v4.masterworker.exceptions;

public class JobDoesNotExistException extends MasterWorkerException {
    public JobDoesNotExistException() {
    }

    public JobDoesNotExistException(String s) {
        super(s);
    }

    public JobDoesNotExistException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JobDoesNotExistException(Throwable throwable) {
        super(throwable);
    }
}
