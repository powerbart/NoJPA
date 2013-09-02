package dk.lessismore.nojpa.net.link;

import java.io.IOException;

public class WriteTimeoutException extends IOException {

    public WriteTimeoutException() {
        super();
    }

    public WriteTimeoutException(String s) {
        super(s);
    }
}
