package dk.lessismore.nojpa.masterworker.exceptions;

public class MasterUnreachableException extends MasterWorkerException {
    public MasterUnreachableException() {
    }

    public MasterUnreachableException(String s) {
        super(s);
    }

    public MasterUnreachableException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MasterUnreachableException(Throwable throwable) {
        super(throwable);
    }
}
