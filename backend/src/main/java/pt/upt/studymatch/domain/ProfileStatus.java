package pt.upt.studymatch.domain;

/**
 * Which kinds of evidence a competency score is based on.
 */
public enum ProfileStatus {
    /** No evidence at all: the score is unknown (never 0). */
    UNKNOWN,
    /** Only cold-start evidence (teacher assessment). */
    INITIAL_ONLY,
    /** Only academic results. */
    ACADEMIC,
    /** Academic results combined with cold-start evidence. */
    BLENDED
}
