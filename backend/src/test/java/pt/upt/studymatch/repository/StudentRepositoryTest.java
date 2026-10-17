package pt.upt.studymatch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DegreeRepository degreeRepository;

    @Test
    void savedStudentCanBeFoundByNumberAndEmail() {
        Degree lei = degreeRepository.save(new Degree("LEI", "Engenharia Informática"));
        Student saved = studentRepository.save(new Student("51631", "Miguel Leitão", "51631@alunos.upt.pt", lei, 2025));

        assertThat(studentRepository.existsByStudentNumber("51631")).isTrue();
        assertThat(studentRepository.existsByStudentNumber("99999")).isFalse();
        assertThat(studentRepository.existsByEmailIgnoreCase("51631@ALUNOS.upt.pt")).isTrue();
        assertThat(studentRepository.existsByStudentNumberAndIdNot("51631", saved.getId())).isFalse();
        assertThat(studentRepository.findByStudentNumber("51631")).contains(saved);
    }
}
