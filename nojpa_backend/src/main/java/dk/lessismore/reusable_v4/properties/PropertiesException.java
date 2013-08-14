package dk.lessismore.reusable_v4.properties;

/**
 * Thrown whenever there is a problem with the interface or the properties file.
 * It is typically only worth catching if you want to report a configuration error to the end user.
 */
public class PropertiesException extends RuntimeException {
    public PropertiesException() {
    }

    public PropertiesException(String s) {
        super(s);
    }

    public PropertiesException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PropertiesException(Throwable throwable) {
        super(throwable);
    }
}
