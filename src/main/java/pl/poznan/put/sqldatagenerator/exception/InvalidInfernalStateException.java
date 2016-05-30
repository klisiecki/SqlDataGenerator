package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class InvalidInfernalStateException extends RuntimeException {
    public InvalidInfernalStateException(String message) {
        super("InvalidInternalStateException: " + message);
    }

    public InvalidInfernalStateException() {
        super();
    }
}