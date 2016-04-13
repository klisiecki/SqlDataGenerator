package pl.poznan.put.SqlDataGenerator.exception;

// TODO rethink name
public class SQLSyntaxNotImplementedException extends RuntimeException {
    public SQLSyntaxNotImplementedException(String message) {
        super(message);
    }
}