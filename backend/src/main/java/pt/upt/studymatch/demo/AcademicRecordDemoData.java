package pt.upt.studymatch.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * Demo academic history. Each demo student's records are only added if that student has
 * no records yet.
 */
@Component
@Order(40)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class AcademicRecordDemoData implements ApplicationRunner {

    private static final RecordStatus PASSED = RecordStatus.PASSED;
    private static final RecordStatus FAILED = RecordStatus.FAILED;
    private static final RecordStatus ENROLLED = RecordStatus.ENROLLED;

    private final AcademicRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final CourseUnitRepository courseUnitRepository;

    public AcademicRecordDemoData(AcademicRecordRepository recordRepository, StudentRepository studentRepository,
            CourseUnitRepository courseUnitRepository) {
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.courseUnitRepository = courseUnitRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Diogo, 3rd year: failed Matemática Discreta once and passed it the following year.
        studentRecords("50112",
                new Row("IP", "2024/2025", 1, PASSED, 16),
                new Row("MD", "2024/2025", 1, FAILED, 7),
                new Row("POO", "2024/2025", 2, PASSED, 15),
                new Row("AED", "2025/2026", 1, PASSED, 13),
                new Row("BD", "2025/2026", 1, PASSED, 17),
                new Row("MD", "2025/2026", 1, PASSED, 11),
                new Row("ES", "2025/2026", 2, PASSED, 15),
                new Row("RC", "2025/2026", 2, PASSED, 12),
                new Row("QS", "2026/2027", 1, ENROLLED, null));
        // Ana, 2nd year: first year graded, currently enrolled in the second year.
        studentRecords("52031",
                new Row("IP", "2025/2026", 1, PASSED, 17),
                new Row("MD", "2025/2026", 1, FAILED, 8),
                new Row("POO", "2025/2026", 2, PASSED, 15),
                new Row("AED", "2026/2027", 1, ENROLLED, null),
                new Row("BD", "2026/2027", 1, ENROLLED, null),
                new Row("MD", "2026/2027", 1, ENROLLED, null));
        // Bruno, 1st year: enrolled, no grades yet.
        studentRecords("53007",
                new Row("IP", "2026/2027", 1, ENROLLED, null),
                new Row("MD", "2026/2027", 1, ENROLLED, null));
    }

    private void studentRecords(String studentNumber, Row... rows) {
        Student student = studentRepository.findByStudentNumber(studentNumber).orElse(null);
        if (student == null || recordRepository.existsByStudentId(student.getId())) {
            return;
        }
        for (Row row : rows) {
            CourseUnit unit = courseUnitRepository.findByCode(row.courseUnitCode()).orElseThrow();
            recordRepository.save(new AcademicRecord(student, unit, row.academicYear(), row.semester(),
                    row.status(), row.grade()));
        }
    }

    private record Row(String courseUnitCode, String academicYear, int semester, RecordStatus status, Integer grade) {
    }
}
