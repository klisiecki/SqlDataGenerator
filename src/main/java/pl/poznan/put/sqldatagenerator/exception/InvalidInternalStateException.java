package pl.poznan.put.sqldatagenerator.exception;

public class InvalidInternalStateException extends RuntimeException {
    public InvalidInternalStateException(String message) {
        super(message);
    }

    public InvalidInternalStateException() {
        super();
    }
}