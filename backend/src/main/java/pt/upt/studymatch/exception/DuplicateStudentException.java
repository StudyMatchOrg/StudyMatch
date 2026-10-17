package pt.upt.studymatch.exception;

public class DuplicateStudentException extends ConflictException {

    public DuplicateStudentException(String message) {
        super(message);
    }
}
