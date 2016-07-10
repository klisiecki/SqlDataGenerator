package pl.poznan.put.sqldatagenerator.exception;

public class SQLNotCompatibleWithDatabaseException extends RuntimeException {
    public SQLNotCompatibleWithDatabaseException(String message) {
        super(message);
    }

    public SQLNotCompatibleWithDatabaseException() {
        super();
    }
}