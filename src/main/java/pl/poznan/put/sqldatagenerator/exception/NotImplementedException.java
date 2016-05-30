package pl.poznan.put.sqldatagenerator.exception;

// TODO rethink name
public class NotImplementedException extends RuntimeException {
    public NotImplementedException(String message) {
        super("NotImplementedException: " + message);
    }

    public NotImplementedException() {
        super();
    }
}