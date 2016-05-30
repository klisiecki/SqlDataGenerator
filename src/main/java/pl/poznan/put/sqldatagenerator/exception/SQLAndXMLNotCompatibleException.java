package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class SQLAndXMLNotCompatibleException extends RuntimeException {
    public SQLAndXMLNotCompatibleException(String message) {
        super("SQLAndXMLNotCompatibleException: " + message);
    }

    public SQLAndXMLNotCompatibleException() {
        super();
    }
}