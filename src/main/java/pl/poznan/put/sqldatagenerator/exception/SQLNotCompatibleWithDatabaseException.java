package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class SQLNotCompatibleWithDatabaseException extends RuntimeException {
    public SQLNotCompatibleWithDatabaseException(String message) {
        super(message);
    }

    public SQLNotCompatibleWithDatabaseException() {
        super();
    }
}