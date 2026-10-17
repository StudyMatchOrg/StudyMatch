package pt.upt.studymatch.exception;

// Returns 404 Not Found for missing resources
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
