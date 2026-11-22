package pt.upt.studymatch.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.domain.UserAccount;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.GroupingContextRepository;
import pt.upt.studymatch.repository.StudentRepository;
import pt.upt.studymatch.repository.UserAccountRepository;

/**
 * A realistic class for group formation: 22 second-year students who, with Ana Sousa, are the 23
 * students enrolled in Algoritmos e Estruturas de Dados in 2026/2027. Their first-year grades vary
 * (strong programmers, strong mathematicians, all-rounders, students who struggle), and three of
 * them transferred from another university, so the system has no grades for them (cold start).
 *
 * <p>Also adds the project "AED 2026/27 – Project groups", owned by the demo teacher. 23 students
 * in groups of 5 give 5 + 5 + 5 + 4 + 4, the example in the Sprint 3 brief. Each student and the
 * project are only added if missing.
 */
@Component
@Order(60)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class GroupingDemoData implements ApplicationRunner {

    static final String PROJECT_NAME = "AED 2026/27 – Project groups";
    private static final String FIRST_YEAR = "2025/2026";
    private static final String CURRENT_YEAR = "2026/2027";
    private static final LocalDate ASSESSED_ON = LocalDate.of(2026, 10, 16);

    private final StudentRepository studentRepository;
    private final DegreeRepository degreeRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final AcademicRecordRepository recordRepository;
    private final CompetencyRepository competencyRepository;
    private final CompetencyAssessmentRepository assessmentRepository;
    private final GroupingContextRepository contextRepository;
    private final UserAccountRepository accountRepository;

    public GroupingDemoData(StudentRepository studentRepository, DegreeRepository degreeRepository,
            CourseUnitRepository courseUnitRepository, AcademicRecordRepository recordRepository,
            CompetencyRepository competencyRepository, CompetencyAssessmentRepository assessmentRepository,
            GroupingContextRepository contextRepository, UserAccountRepository accountRepository) {
        this.studentRepository = studentRepository;
        this.degreeRepository = degreeRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.recordRepository = recordRepository;
        this.competencyRepository = competencyRepository;
        this.assessmentRepository = assessmentRepository;
        this.contextRepository = contextRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Number, name, then the first-year grades in IP, MD and POO (below 10 is a failed attempt).
        student("52040", "João Ribeiro", 18, 9, 17);
        student("52041", "Mariana Costa", 12, 18, 11);
        student("52042", "Tiago Martins", 15, 14, 16);
        student("52043", "Inês Carvalho", 19, 16, 18);
        student("52044", "Pedro Gomes", 10, 10, 10);
        student("52045", "Beatriz Rocha", 13, 17, 12);
        student("52046", "Rafael Lopes", 17, 11, 15);
        student("52047", "Sofia Pinto", 11, 19, 13);
        student("52048", "Gonçalo Fernandes", 14, 12, 14);
        student("52049", "Leonor Antunes", 16, 15, 17);
        student("52050", "Miguel Teixeira", 10, 8, 11);
        student("52051", "Catarina Moreira", 18, 18, 19);
        student("52052", "André Sousa", 12, 13, 10);
        student("52053", "Matilde Correia", 15, 10, 14);
        student("52054", "Rodrigo Mendes", 13, 15, 12);
        student("52055", "Francisca Nunes", 17, 13, 16);
        student("52056", "Henrique Vieira", 11, 16, 12);
        student("52057", "Carolina Marques", 14, 14, 13);
        student("52058", "Vasco Barbosa", 16, 9, 15);
        // Transferred students: no grades here, so the profile relies on the teacher (cold start).
        transferStudent("52059", "Lara Azevedo", Map.of("PROG", 0.8, "PROB", 0.7));
        transferStudent("52060", "Tomás Freitas", Map.of("MATH", 0.6));
        transferStudent("52061", "Alice Monteiro", Map.of());
        project();
    }

    private void student(String number, String name, int ip, int md, int poo) {
        Student student = newStudent(number, name);
        if (student == null) {
            return;
        }
        record(student, "IP", FIRST_YEAR, 1, ip);
        record(student, "MD", FIRST_YEAR, 1, md);
        record(student, "POO", FIRST_YEAR, 2, poo);
        enrolInAed(student);
    }

    private void transferStudent(String number, String name, Map<String, Double> teacherAssessment) {
        Student student = newStudent(number, name);
        if (student == null) {
            return;
        }
        enrolInAed(student);
        teacherAssessment.forEach((code, value) -> competencyRepository.findByCode(code).ifPresent(competency ->
                assessmentRepository.save(new CompetencyAssessment(student, competency, value, ASSESSED_ON))));
    }

    /** Returns the new student, or null if a student with this number already exists. */
    private Student newStudent(String number, String name) {
        if (studentRepository.existsByStudentNumber(number)) {
            return null;
        }
        Degree lei = degreeRepository.findByCode("LEI").orElseThrow();
        return studentRepository.save(new Student(number, name, number + "@alunos.upt.pt", lei, 2025));
    }

    private void record(Student student, String courseUnitCode, String year, int semester, int grade) {
        RecordStatus status = grade >= AcademicRecord.PASSING_GRADE ? RecordStatus.PASSED : RecordStatus.FAILED;
        recordRepository.save(new AcademicRecord(student, courseUnit(courseUnitCode), year, semester, status, grade));
    }

    private void enrolInAed(Student student) {
        recordRepository.save(new AcademicRecord(student, courseUnit("AED"), CURRENT_YEAR, 1, RecordStatus.ENROLLED,
                null));
    }

    private void project() {
        if (contextRepository.findFirstByName(PROJECT_NAME).isPresent()) {
            return;
        }
        UserAccount teacher = accountRepository.findByUsername("teacher").orElse(null);
        if (teacher == null) {
            return;
        }
        CourseUnit aed = courseUnit("AED");
        GroupingContext project = new GroupingContext(PROJECT_NAME,
                "Groups for the AED practical project: implementing and analysing data structures.", aed,
                CURRENT_YEAR, 5, teacher, LocalDateTime.of(2026, 11, 2, 10, 0));
        List<Student> enrolled = contextRepository.findEnrolledStudents(aed.getId(), CURRENT_YEAR);
        project.replaceStudents(enrolled);
        Map<Competency, Double> importance = new LinkedHashMap<>();
        importance(importance, "PROG", 1.0);
        importance(importance, "PROB", 0.8);
        importance(importance, "MATH", 0.6);
        importance(importance, "SWENG", 0.4);
        project.replaceCompetencies(importance);
        contextRepository.save(project);
    }

    private void importance(Map<Competency, Double> importance, String code, double value) {
        competencyRepository.findByCode(code).ifPresent(competency -> importance.put(competency, value));
    }

    private CourseUnit courseUnit(String code) {
        return courseUnitRepository.findByCode(code).orElseThrow();
    }
}
