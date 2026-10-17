package pt.upt.studymatch.exception;

// Returns 400 Bad Request for business rule violations
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
