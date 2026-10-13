package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.StudentRequest;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.exception.DuplicateStudentException;
import pt.upt.studymatch.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void registerSavesNormalisedStudent() {
        when(studentRepository.save(any(Student.class))).thenAnswer(call -> call.getArgument(0));

        StudentResponse response = studentService.register(
                new StudentRequest(" 51631 ", " Miguel Leitão ", "51631@Alunos.UPT.pt"));

        assertThat(response.studentNumber()).isEqualTo("51631");
        assertThat(response.name()).isEqualTo("Miguel Leitão");
        assertThat(response.email()).isEqualTo("51631@alunos.upt.pt");
    }

    @Test
    void registerRejectsDuplicateStudentNumber() {
        when(studentRepository.existsByStudentNumber("51631")).thenReturn(true);

        assertThatThrownBy(() -> studentService.register(
                new StudentRequest("51631", "Miguel Leitão", "51631@alunos.upt.pt")))
                .isInstanceOf(DuplicateStudentException.class)
                .hasMessageContaining("51631");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(studentRepository.existsByEmailIgnoreCase("51631@alunos.upt.pt")).thenReturn(true);

        assertThatThrownBy(() -> studentService.register(
                new StudentRequest("51634", "Guilherme Barros", "51631@alunos.upt.pt")))
                .isInstanceOf(DuplicateStudentException.class)
                .hasMessageContaining("Email");
        verify(studentRepository, never()).save(any());
    }
}
