package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class InvalidInternalStateException extends RuntimeException {
    public InvalidInternalStateException(String message) {
        super("InvalidInternalStateException: " + message);
    }

    public InvalidInternalStateException() {
        super();
    }
}