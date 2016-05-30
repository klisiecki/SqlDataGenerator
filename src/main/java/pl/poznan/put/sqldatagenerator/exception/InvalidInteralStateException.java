package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class InvalidInteralStateException extends RuntimeException {
    public InvalidInteralStateException(String message) {
        super("InvalidInteralStateException: " + message);
    }

    public InvalidInteralStateException() {
        super();
    }
}