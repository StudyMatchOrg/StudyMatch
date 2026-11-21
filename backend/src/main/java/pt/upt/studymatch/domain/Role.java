package pt.upt.studymatch.domain;

/**
 * What an account can do. An admin can do everything a teacher can, plus manage accounts, the
 * academic structure (degrees and course units) and students.
 */
public enum Role {
    ADMIN,
    TEACHER
}
