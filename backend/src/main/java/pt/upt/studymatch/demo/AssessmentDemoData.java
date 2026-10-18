package pt.upt.studymatch.demo;

import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.CompetencyAssessmentRepository;
import pt.upt.studymatch.repository.CompetencyRepository;
import pt.upt.studymatch.repository.StudentRepository;

/**
 * Demo cold start: Bruno Costa, a first-year student without grades, has a teacher's initial
 * assessment of three competencies. Only added if Bruno has no assessments yet.
 */
@Component
@Order(50)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class AssessmentDemoData implements ApplicationRunner {

    private final CompetencyAssessmentRepository assessmentRepository;
    private final CompetencyRepository competencyRepository;
    private final StudentRepository studentRepository;

    public AssessmentDemoData(CompetencyAssessmentRepository assessmentRepository,
            CompetencyRepository competencyRepository, StudentRepository studentRepository) {
        this.assessmentRepository = assessmentRepository;
        this.competencyRepository = competencyRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Student bruno = studentRepository.findByStudentNumber("53007").orElse(null);
        if (bruno == null || assessmentRepository.existsByStudentId(bruno.getId())) {
            return;
        }
        LocalDate assessedOn = LocalDate.of(2026, 10, 9);
        assess(bruno, "PROG", 0.6, assessedOn);
        assess(bruno, "MATH", 0.7, assessedOn);
        assess(bruno, "PROB", 0.5, assessedOn);
    }

    private void assess(Student student, String competencyCode, double value, LocalDate assessedOn) {
        competencyRepository.findByCode(competencyCode).ifPresent(competency ->
                assessmentRepository.save(new CompetencyAssessment(student, competency, value, assessedOn)));
    }
}
