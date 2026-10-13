package pt.upt.studymatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.domain.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmailIgnoreCase(String email);
}
