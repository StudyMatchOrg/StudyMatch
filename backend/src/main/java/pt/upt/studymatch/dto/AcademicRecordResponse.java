package pt.upt.studymatch.dto;

import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.RecordStatus;

public record AcademicRecordResponse(
        Long id, CourseUnitSummary courseUnit, String academicYear, int semester, RecordStatus status,
        Integer grade) {

    public record CourseUnitSummary(Long id, String code, String name, double ects) {
    }

    public static AcademicRecordResponse from(AcademicRecord record) {
        CourseUnit unit = record.getCourseUnit();
        return new AcademicRecordResponse(record.getId(),
                new CourseUnitSummary(unit.getId(), unit.getCode(), unit.getName(), unit.getEcts()),
                record.getAcademicYear(), record.getSemester(), record.getStatus(), record.getGrade());
    }
}
