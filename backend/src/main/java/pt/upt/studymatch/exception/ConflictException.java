package pt.upt.studymatch.exception;

// Returns 409 Conflict for duplicates or data clashes
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
