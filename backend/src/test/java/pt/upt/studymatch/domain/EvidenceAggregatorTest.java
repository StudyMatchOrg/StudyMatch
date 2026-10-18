package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.domain.Competency;
import pt.upt.studymatch.domain.CompetencyAssessment;
import pt.upt.studymatch.domain.Degree;
import pt.upt.studymatch.domain.Student;
import pt.upt.studymatch.exception.BusinessRuleException;

class ColdStartPolicyTest {

    private static final Student BRUNO = new Student("53007", "Bruno Costa", "53007@alunos.upt.pt",
            new Degree("LEI", "Engenharia Informática"), 2026);
    private static final Competency PROGRAMMING = new Competency("PROG", "Programming", null);
    private static final LocalDate DATE = LocalDate.of(2026, 10, 9);

    @Test
    void teacherAssessmentIsWorthOneStandardCourseUnit() {
        Evidence evidence = new ColdStartPolicy().toEvidence(new CompetencyAssessment(BRUNO, PROGRAMMING, 0.6, DATE));

        assertThat(evidence.type()).isEqualTo(EvidenceType.TEACHER_ASSESSMENT);
        assertThat(evidence.value()).isEqualTo(0.6);
        assertThat(evidence.weight()).isEqualTo(1.0);
        assertThat(evidence.label()).contains("2026-10-09");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0})
    void assessmentValuesAtTheLimitsAreAccepted(double value) {
        assertThat(new CompetencyAssessment(BRUNO, PROGRAMMING, value, DATE).getValue()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 1.01})
    void assessmentValuesOutsideZeroToOneAreRejected(double value) {
        assertThatThrownBy(() -> new CompetencyAssessment(BRUNO, PROGRAMMING, value, DATE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("between 0 and 1");
    }

    @Test
    void evidenceNeedsAPositiveWeight() {
        assertThatThrownBy(() -> new Evidence(EvidenceType.ACADEMIC_RESULT, "IP", 0.5, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
