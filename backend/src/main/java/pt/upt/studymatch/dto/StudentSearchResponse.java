package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.Student;

/**
 * A student in the search results: the usual student data, plus the score in the filtered
 * competency and whether the profile has no grades yet, when the search needed the profiles.
 */
public record StudentSearchResponse(Long id, String studentNumber, String name, String email, DegreeResponse degree,
        int enrolmentYear, Double competencyScore, Boolean lowEvidence) {

    public static StudentSearchResponse from(Student student, Double competencyScore, Boolean lowEvidence) {
        return new StudentSearchResponse(student.getId(), student.getStudentNumber(), student.getName(),
                student.getEmail(), DegreeResponse.from(student.getDegree()), student.getEnrolmentYear(),
                competencyScore, lowEvidence);
    }
}
