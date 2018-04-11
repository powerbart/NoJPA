package dk.lessismore.nojpa.masterworker.exceptions;

/**
 * Created by seb on 07/09/2017.
 */
public class TimeoutException extends RuntimeException {


    public TimeoutException() {
    }

    public TimeoutException(String s) {
        super(s);
    }

    public TimeoutException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TimeoutException(Throwable throwable) {
        super(throwable);
    }

}
