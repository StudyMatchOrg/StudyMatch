package pt.upt.studymatch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import pt.upt.studymatch.domain.Student;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void savedStudentCanBeFoundByNumberAndEmail() {
        studentRepository.save(new Student("51631", "Miguel Leitão", "51631@alunos.upt.pt"));

        assertThat(studentRepository.existsByStudentNumber("51631")).isTrue();
        assertThat(studentRepository.existsByStudentNumber("99999")).isFalse();
        assertThat(studentRepository.existsByEmailIgnoreCase("51631@ALUNOS.upt.pt")).isTrue();
    }
}
