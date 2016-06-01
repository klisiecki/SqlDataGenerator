package pl.poznan.put.sqldatagenerator.exception;

public class SQLInvalidSyntaxException extends RuntimeException {
    public SQLInvalidSyntaxException(String message) {
        super(message);
    }

    public SQLInvalidSyntaxException() {
        super();
    }
}