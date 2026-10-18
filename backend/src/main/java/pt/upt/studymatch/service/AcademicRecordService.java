package pt.upt.studymatch.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.AcademicRecordChange;
import pt.upt.studymatch.dto.AcademicRecordRequest;
import pt.upt.studymatch.dto.AcademicRecordResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;

@Service
public class AcademicRecordService {

    private final AcademicRecordRepository recordRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final StudentService studentService;

    public AcademicRecordService(AcademicRecordRepository recordRepository, CourseUnitRepository courseUnitRepository,
            StudentService studentService) {
        this.recordRepository = recordRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.studentService = studentService;
    }

    @Transactional(readOnly = true)
    public List<AcademicRecordResponse> findByStudent(Long studentId) {
        studentService.requireStudent(studentId);
        return recordRepository.findByStudentInOrder(studentId).stream().map(AcademicRecordResponse::from).toList();
    }

    @Transactional
    public AcademicRecordResponse add(Long studentId, AcademicRecordRequest request) {
        Student student = studentService.requireStudent(studentId);
        CourseUnit courseUnit = courseUnitRepository.findById(request.courseUnitId())
                .orElseThrow(() -> new BusinessRuleException("Course unit " + request.courseUnitId() + " does not exist"));
        return AcademicRecordResponse.from(
                record(student, courseUnit, request.academicYear(), request.semester(), request.status(), request.grade()));
    }

    /**
     * Records an attempt of a student at a course unit, applying all academic-history rules.
     * Used by the form and by the CSV import.
     */
    @Transactional
    public AcademicRecord record(Student student, CourseUnit courseUnit, String academicYear, int semester,
            RecordStatus status, Integer grade) {
        String year = academicYear.trim();
        if (recordRepository.existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemester(
                student.getId(), courseUnit.getId(), year, semester)) {
            throw duplicateAttempt(courseUnit, year, semester);
        }
        return recordRepository.save(new AcademicRecord(student, courseUnit, year, semester, status, grade));
    }

    @Transactional
    public AcademicRecordResponse change(Long recordId, AcademicRecordChange change) {
        AcademicRecord record = requireRecord(recordId);
        String year = change.academicYear().trim();
        if (recordRepository.existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemesterAndIdNot(
                record.getStudent().getId(), record.getCourseUnit().getId(), year, change.semester(), recordId)) {
            throw duplicateAttempt(record.getCourseUnit(), year, change.semester());
        }
        record.change(year, change.semester(), change.status(), change.grade());
        return AcademicRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long recordId) {
        recordRepository.delete(requireRecord(recordId));
    }

    private AcademicRecord requireRecord(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Academic record " + recordId + " does not exist"));
    }

    private static ConflictException duplicateAttempt(CourseUnit courseUnit, String academicYear, int semester) {
        return new ConflictException("There is already a record for " + courseUnit.getCode() + " in "
                + academicYear + ", semester " + semester);
    }
}
