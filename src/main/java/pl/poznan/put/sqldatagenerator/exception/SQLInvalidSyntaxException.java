package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class SQLInvalidSyntaxException extends RuntimeException {
    public SQLInvalidSyntaxException(String message) {
        super("SQLInvalidSyntaxException: " + message);
    }

    public SQLInvalidSyntaxException() {
        super();
    }
}