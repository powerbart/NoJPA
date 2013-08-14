package dk.lessismore.reusable_v4.masterworker.exceptions;

public class WrappedErrorException extends RuntimeException {
    public WrappedErrorException(Throwable throwable) {
        super(throwable);
    }
}
