package pt.upt.studymatch.exception;

// Returns 401 Unauthorized: wrong credentials, or no valid login
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
