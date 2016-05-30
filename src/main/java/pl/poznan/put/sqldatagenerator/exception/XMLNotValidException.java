package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class XMLNotValidException extends RuntimeException {
    public XMLNotValidException(String message) {
        super("XMLNotValidException: " + message);
    }

    public XMLNotValidException() {
        super();
    }
}