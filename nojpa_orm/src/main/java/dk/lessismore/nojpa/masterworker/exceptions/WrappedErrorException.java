package dk.lessismore.nojpa.masterworker.exceptions;

public class WrappedErrorException extends RuntimeException {
    public WrappedErrorException(Throwable throwable) {
        super(throwable);
    }
}
