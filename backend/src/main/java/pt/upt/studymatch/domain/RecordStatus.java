package pt.upt.studymatch.domain;

/**
 * State of a student's attempt at a course unit.
 */
public enum RecordStatus {
    /** Currently taking the course unit: no grade yet. */
    ENROLLED,
    /** Finished with a grade of at least 10. */
    PASSED,
    /** Finished with a grade below 10. */
    FAILED
}
