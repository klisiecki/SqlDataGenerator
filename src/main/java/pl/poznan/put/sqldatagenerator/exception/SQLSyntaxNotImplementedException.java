package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class SQLSyntaxNotImplementedException extends RuntimeException {
    public SQLSyntaxNotImplementedException(String message) {
        super(message);
    }
}