package pl.poznan.put.sqldatagenerator.exception;

public class XMLNotValidException extends RuntimeException {
    public XMLNotValidException(String message) {
        super(message);
    }

    public XMLNotValidException() {
        super();
    }
}