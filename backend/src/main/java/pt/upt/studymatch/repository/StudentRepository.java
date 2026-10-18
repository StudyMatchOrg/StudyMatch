package pt.upt.studymatch.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.domain.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentNumberAndIdNot(String studentNumber, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<Student> findByStudentNumber(String studentNumber);
}
