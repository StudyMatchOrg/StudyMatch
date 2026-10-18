package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.Student;

public record StudentResponse(
        Long id, String studentNumber, String name, String email, DegreeResponse degree, int enrolmentYear) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(student.getId(), student.getStudentNumber(), student.getName(),
                student.getEmail(), DegreeResponse.from(student.getDegree()), student.getEnrolmentYear());
    }
}
