package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AcademicEvidenceTest {

    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final Student DIOGO = new Student("50112", "Diogo Pereira", "50112@alunos.upt.pt", LEI, 2024);
    private static final CourseUnit MD = new CourseUnit("MD", "Matemática Discreta", 6, 1, 1, Set.of(LEI));
    private static final CourseUnit POO = new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(LEI));
    private static final Competency MATH = new Competency("MATH", "Mathematical Reasoning", null);
    private static final Competency PROG = new Competency("PROG", "Programming", null);
    private static final Competency SWENG = new Competency("SWENG", "Software Engineering", null);

    private final AcademicEvidence academicEvidence = new AcademicEvidence();

    private static AcademicRecord record(CourseUnit unit, String year, RecordStatus status, Integer grade) {
        return new AcademicRecord(DIOGO, unit, year, unit.getSemester(), status, grade);
    }

    @Test
    void passedRetakeReplacesTheFailedAttempt() {
        List<AcademicRecord> records = List.of(
                record(MD, "2024/2025", RecordStatus.FAILED, 7),
                record(MD, "2025/2026", RecordStatus.PASSED, 11));

        assertThat(academicEvidence.bestAttempts(records).get("MD").getGrade()).isEqualTo(11);
    }

    @Test
    void courseUnitThatWasOnlyFailedStillCounts() {
        Map<String, List<Evidence>> evidence = academicEvidence.byCompetency(
                List.of(record(MD, "2024/2025", RecordStatus.FAILED, 8)),
                List.of(new CourseUnitCompetency(MD, MATH, 1.0)));

        assertThat(evidence.get("MATH")).singleElement()
                .satisfies(e -> assertThat(e.value()).isCloseTo(0.4, within(0.001)));
    }

    @Test
    void enrolledRecordsAreNotEvidence() {
        Map<String, List<Evidence>> evidence = academicEvidence.byCompetency(
                List.of(record(MD, "2026/2027", RecordStatus.ENROLLED, null)),
                List.of(new CourseUnitCompetency(MD, MATH, 1.0)));

        assertThat(evidence).isEmpty();
    }

    @Test
    void weightIsCompetencyWeightTimesEctsOverSix() {
        Map<String, List<Evidence>> evidence = academicEvidence.byCompetency(
                List.of(record(POO, "2024/2025", RecordStatus.PASSED, 15)),
                List.of(new CourseUnitCompetency(POO, PROG, 1.0), new CourseUnitCompetency(POO, SWENG, 0.4)));

        assertThat(evidence.get("PROG").get(0).weight()).isCloseTo(1.25, within(0.001));
        assertThat(evidence.get("SWENG").get(0).weight()).isCloseTo(0.5, within(0.001));
        assertThat(evidence.get("PROG").get(0).value()).isCloseTo(0.75, within(0.001));
        assertThat(evidence.get("PROG").get(0).label()).contains("POO").contains("grade 15");
    }
}
