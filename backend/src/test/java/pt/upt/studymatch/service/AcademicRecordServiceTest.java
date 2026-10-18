package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.upt.studymatch.domain.AcademicRecord;
import pt.upt.studymatch.domain.CourseUnit;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.dto.AcademicRecordChange;
import pt.upt.studymatch.dto.AcademicRecordRequest;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.exception.ConflictException;
import pt.upt.studymatch.exception.NotFoundException;
import pt.upt.studymatch.repository.AcademicRecordRepository;
import pt.upt.studymatch.repository.CourseUnitRepository;

@ExtendWith(MockitoExtension.class)
class AcademicRecordServiceTest {

    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final Student ANA = new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", LEI, 2025);
    private static final CourseUnit MD = new CourseUnit("MD", "Matemática Discreta", 6, 1, 1, Set.of(LEI));

    @Mock
    private AcademicRecordRepository recordRepository;

    @Mock
    private CourseUnitRepository courseUnitRepository;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private AcademicRecordService recordService;

    @Test
    void retakeInALaterYearIsAccepted() {
        when(recordRepository.save(any(AcademicRecord.class))).thenAnswer(call -> call.getArgument(0));

        recordService.record(ANA, MD, "2025/2026", 1, RecordStatus.FAILED, 8);
        AcademicRecord retake = recordService.record(ANA, MD, "2026/2027", 1, RecordStatus.PASSED, 12);

        assertThat(retake.getGrade()).isEqualTo(12);
    }

    @Test
    void secondRecordInTheSameSemesterIsAConflict() {
        when(recordRepository.existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemester(any(), any(),
                any(), any(Integer.class))).thenReturn(true);

        assertThatThrownBy(() -> recordService.record(ANA, MD, "2025/2026", 1, RecordStatus.PASSED, 12))
                .isInstanceOf(ConflictException.class)
                .hasMessage("There is already a record for MD in 2025/2026, semester 1");
        verify(recordRepository, never()).save(any());
    }

    @Test
    void unknownCourseUnitIsRejected() {
        when(studentService.requireStudent(1L)).thenReturn(ANA);
        when(courseUnitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.add(1L,
                new AcademicRecordRequest(99L, "2025/2026", 1, RecordStatus.PASSED, 12)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Course unit 99 does not exist");
    }

    @Test
    void changingAGradeKeepsTheSameRecord() {
        AcademicRecord record = new AcademicRecord(ANA, MD, "2025/2026", 1, RecordStatus.FAILED, 8);
        when(recordRepository.findById(5L)).thenReturn(Optional.of(record));

        recordService.change(5L, new AcademicRecordChange("2025/2026", 1, RecordStatus.PASSED, 13));

        assertThat(record.getStatus()).isEqualTo(RecordStatus.PASSED);
        assertThat(record.getGrade()).isEqualTo(13);
    }

    @Test
    void unknownRecordIsNotFound() {
        when(recordRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.delete(5L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Academic record 5 does not exist");
    }
}
