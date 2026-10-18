package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.exception.BusinessRuleException;

class AcademicRecordTest {

    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final Degree LIG = new Degree("LIG", "Informática de Gestão");
    private static final Student ANA = new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", LEI, 2025);
    private static final CourseUnit POO = new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(LEI));

    private static AcademicRecord record(RecordStatus status, Integer grade) {
        return new AcademicRecord(ANA, POO, "2025/2026", 2, status, grade);
    }

    // Invalid scenarios

    @Test
    void gradeAboveTwentyIsRejected() {
        assertThatThrownBy(() -> record(RecordStatus.PASSED, 21))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Grade must be between 0 and 20, but was 21");
    }

    @Test
    void passedRecordWithFailingGradeIsRejected() {
        assertThatThrownBy(() -> record(RecordStatus.PASSED, 8))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at least 10");
    }

    @Test
    void enrolledRecordCannotHaveAGrade() {
        assertThatThrownBy(() -> record(RecordStatus.ENROLLED, 14))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cannot have a grade");
    }

    @Test
    void finishedRecordNeedsAGrade() {
        assertThatThrownBy(() -> record(RecordStatus.PASSED, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("needs a grade");
    }

    @Test
    void courseUnitMustBelongToTheStudentsDegree() {
        CourseUnit si = new CourseUnit("SI", "Sistemas de Informação", 6, 2, 2, Set.of(LIG));

        assertThatThrownBy(() -> new AcademicRecord(ANA, si, "2025/2026", 2, RecordStatus.PASSED, 14))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not part of the degree LEI");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025-2026", "2025/2027", "25/26", ""})
    void academicYearMustLookLikeTwoConsecutiveYears(String academicYear) {
        assertThatThrownBy(() -> new AcademicRecord(ANA, POO, academicYear, 2, RecordStatus.PASSED, 14))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Academic year");
    }

    @Test
    void semesterMustBeOneOrTwo() {
        assertThatThrownBy(() -> new AcademicRecord(ANA, POO, "2025/2026", 3, RecordStatus.PASSED, 14))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Semester");
    }

    // Boundary conditions

    @ParameterizedTest
    @CsvSource({"PASSED, 10", "PASSED, 20", "FAILED, 9", "FAILED, 0"})
    void gradesAtTheBoundariesAreAccepted(RecordStatus status, int grade) {
        assertThat(record(status, grade).getGrade()).isEqualTo(grade);
    }

    @ParameterizedTest
    @CsvSource({"PASSED, 9", "FAILED, 10", "FAILED, -1"})
    void gradesJustOutsideTheBoundariesAreRejected(RecordStatus status, int grade) {
        assertThatThrownBy(() -> record(status, grade)).isInstanceOf(BusinessRuleException.class);
    }

    // Normal scenario

    @Test
    void enrolledRecordHasNoGradeUntilItIsChanged() {
        AcademicRecord record = record(RecordStatus.ENROLLED, null);
        assertThat(record.hasGrade()).isFalse();

        record.change("2025/2026", 2, RecordStatus.PASSED, 15);

        assertThat(record.hasGrade()).isTrue();
        assertThat(record.getStatus()).isEqualTo(RecordStatus.PASSED);
    }
}
