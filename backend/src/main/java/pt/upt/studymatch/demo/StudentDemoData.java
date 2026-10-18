package pt.upt.studymatch.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

// Demo students for the Sprint 2 review. Skipped if the student number already exists.
@Component
@Order(30)
@ConditionalOnProperty(name = "studymatch.demo-data.enabled", havingValue = "true")
public class StudentDemoData implements ApplicationRunner {

    private final StudentRepository studentRepository;
    private final DegreeRepository degreeRepository;

    public StudentDemoData(StudentRepository studentRepository, DegreeRepository degreeRepository) {
        this.studentRepository = studentRepository;
        this.degreeRepository = degreeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        student("50112", "Diogo Pereira", "LEI", 2024);
        student("52031", "Ana Sousa", "LEI", 2025);
        student("53007", "Bruno Costa", "LEI", 2026);
        student("53015", "Carla Mendes", "LIG", 2026);
    }

    private void student(String number, String name, String degreeCode, int enrolmentYear) {
        if (studentRepository.existsByStudentNumber(number)) {
            return;
        }
        Degree degree = degreeRepository.findByCode(degreeCode).orElseThrow();
        studentRepository.save(new Student(number, name, number + "@alunos.upt.pt", degree, enrolmentYear));
    }
}
