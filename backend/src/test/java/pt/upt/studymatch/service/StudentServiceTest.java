package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.StudentRequest;
import pt.upt.studymatch.dto.StudentResponse;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.DuplicateStudentException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.DegreeRepository;
import pt.upt.studymatch.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final long LEI_ID = 1L;
    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DegreeRepository degreeRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void degreeExists() {
        org.mockito.Mockito.lenient().when(degreeRepository.findById(LEI_ID)).thenReturn(Optional.of(LEI));
    }

    private static StudentRequest request(String number, String name, String email, int enrolmentYear) {
        return new StudentRequest(number, name, email, LEI_ID, enrolmentYear);
    }

    @Test
    void registerSavesNormalisedStudent() {
        when(studentRepository.save(any(Student.class))).thenAnswer(call -> call.getArgument(0));

        StudentResponse response = studentService.register(
                request(" 51631 ", " Miguel Leitão ", "51631@Alunos.UPT.pt", 2025));

        assertThat(response.studentNumber()).isEqualTo("51631");
        assertThat(response.name()).isEqualTo("Miguel Leitão");
        assertThat(response.email()).isEqualTo("51631@alunos.upt.pt");
        assertThat(response.degree().code()).isEqualTo("LEI");
        assertThat(response.enrolmentYear()).isEqualTo(2025);
    }

    @Test
    void registerRejectsDuplicateStudentNumber() {
        when(studentRepository.existsByStudentNumber("51631")).thenReturn(true);

        assertThatThrownBy(() -> studentService.register(
                request("51631", "Miguel Leitão", "51631@alunos.upt.pt", 2025)))
                .isInstanceOf(DuplicateStudentException.class)
                .hasMessageContaining("51631");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(studentRepository.existsByEmailIgnoreCase("51631@alunos.upt.pt")).thenReturn(true);

        assertThatThrownBy(() -> studentService.register(
                request("51634", "Guilherme Barros", "51631@alunos.upt.pt", 2025)))
                .isInstanceOf(DuplicateStudentException.class)
                .hasMessageContaining("Email");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void registerRejectsUnknownDegree() {
        when(degreeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.register(
                new StudentRequest("51631", "Miguel Leitão", "51631@alunos.upt.pt", 99L, 2025)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Degree 99");
    }

    @Test
    void enrolmentYearCanBeTheCurrentYearButNotTheNext() {
        int currentYear = Year.now().getValue();
        when(studentRepository.save(any(Student.class))).thenAnswer(call -> call.getArgument(0));

        assertThat(studentService.register(request("1", "New Student", "1@alunos.upt.pt", currentYear))
                .enrolmentYear()).isEqualTo(currentYear);
        assertThatThrownBy(() -> studentService.register(
                request("2", "Future Student", "2@alunos.upt.pt", currentYear + 1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Enrolment year");
    }

    @Test
    void updateKeepsTheStudentsOwnNumberAndEmail() {
        Student student = new Student("51631", "Miguel", "51631@alunos.upt.pt", LEI, 2025);
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.update(7L,
                request("51631", "Miguel Leitão", "51631@alunos.upt.pt", 2024));

        assertThat(response.name()).isEqualTo("Miguel Leitão");
        assertThat(response.enrolmentYear()).isEqualTo(2024);
    }

    @Test
    void updateRejectsNumberOfAnotherStudent() {
        Student student = new Student("51631", "Miguel", "51631@alunos.upt.pt", LEI, 2025);
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByStudentNumberAndIdNot("51634", 7L)).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(7L,
                request("51634", "Miguel", "51631@alunos.upt.pt", 2025)))
                .isInstanceOf(DuplicateStudentException.class);
    }

    @Test
    void unknownStudentIsNotFound() {
        when(studentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Student 42 does not exist");
    }
}
