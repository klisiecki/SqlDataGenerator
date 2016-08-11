package pl.poznan.put.sqldatagenerator.exception;

public class UnsatisfiableSQLException extends RuntimeException {
    public UnsatisfiableSQLException() {
    }

    public UnsatisfiableSQLException(String message) {
        super(message);
    }
}
