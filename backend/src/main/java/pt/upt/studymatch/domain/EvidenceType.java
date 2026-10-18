package pt.upt.studymatch.domain;

/**
 * Where a piece of evidence about a competency comes from.
 */
public enum EvidenceType {
    /** The best grade of a course unit that develops the competency. */
    ACADEMIC_RESULT,
    /** A teacher's initial estimate, used for the cold start. */
    TEACHER_ASSESSMENT
}
