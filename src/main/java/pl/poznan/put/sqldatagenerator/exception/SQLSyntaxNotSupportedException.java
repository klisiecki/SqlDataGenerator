package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class SQLSyntaxNotSupportedException extends RuntimeException {
    public SQLSyntaxNotSupportedException(String message) {
        super("SQLSyntaxNotSupportedException: " + message);
    }

    public SQLSyntaxNotSupportedException() {
        super();
    }
}