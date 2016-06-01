package pl.poznan.put.sqldatagenerator.exception;

public class SQLSyntaxNotSupportedException extends RuntimeException {
    public SQLSyntaxNotSupportedException(String message) {
        super(message);
    }

    public SQLSyntaxNotSupportedException() {
        super();
    }
}