package pt.upt.studymatch.domain;

/** A student together with their current competency profile. */
public record ProfiledStudent(Student student, StudentProfile profile) {
}
