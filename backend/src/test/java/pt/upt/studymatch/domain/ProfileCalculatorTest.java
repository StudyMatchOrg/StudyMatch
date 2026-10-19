package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Profile rules, using the demo course units, weights and students.
 */
class ProfileCalculatorTest {

    private static final double PRECISION = 0.001;
    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final CourseUnit IP = new CourseUnit("IP", "Introdução à Programação", 6, 1, 1, Set.of(LEI));
    private static final CourseUnit MD = new CourseUnit("MD", "Matemática Discreta", 6, 1, 1, Set.of(LEI));
    private static final CourseUnit POO = new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2, Set.of(LEI));
    private static final CourseUnit AED = new CourseUnit("AED", "Algoritmos e Estruturas de Dados", 7.5, 2, 1, Set.of(LEI));
    private static final Competency PROG = new Competency("PROG", "Programming", null);
    private static final Competency PROB = new Competency("PROB", "Problem Solving", null);
    private static final Competency MATH = new Competency("MATH", "Mathematical Reasoning", null);
    private static final Competency ALGO = new Competency("ALGO", "Algorithms", null);
    private static final List<Competency> COMPETENCIES = List.of(PROG, PROB, MATH, ALGO);
    private static final List<CourseUnitCompetency> LINKS = List.of(
            new CourseUnitCompetency(IP, PROG, 1.0), new CourseUnitCompetency(IP, PROB, 0.6),
            new CourseUnitCompetency(MD, MATH, 1.0), new CourseUnitCompetency(MD, PROB, 0.5),
            new CourseUnitCompetency(POO, PROG, 1.0),
            new CourseUnitCompetency(AED, ALGO, 1.0), new CourseUnitCompetency(AED, PROB, 0.8),
            new CourseUnitCompetency(AED, PROG, 0.5));

    private final ProfileCalculator calculator = new ProfileCalculator();

    private static final Student ANA = new Student("52031", "Ana Sousa", "52031@alunos.upt.pt", LEI, 2025);
    private static final Student BRUNO = new Student("53007", "Bruno Costa", "53007@alunos.upt.pt", LEI, 2026);

    private static List<AcademicRecord> anasRecords() {
        return List.of(
                new AcademicRecord(ANA, IP, "2025/2026", 1, RecordStatus.PASSED, 17),
                new AcademicRecord(ANA, MD, "2025/2026", 1, RecordStatus.FAILED, 8),
                new AcademicRecord(ANA, POO, "2025/2026", 2, RecordStatus.PASSED, 15),
                new AcademicRecord(ANA, AED, "2026/2027", 1, RecordStatus.ENROLLED, null));
    }

    @Test
    void scoreIsTheWeightedAverageOfTheGrades() {
        StudentProfile profile = calculator.calculate(COMPETENCIES, anasRecords(), LINKS, List.of());

        // Programming: IP 17/20 with weight 1.0, POO 15/20 with weight 1.0 × 7.5/6 = 1.25.
        AggregatedScore programming = profile.scoreOf("PROG").orElseThrow();
        assertThat(programming.score()).isCloseTo((0.85 * 1.0 + 0.75 * 1.25) / 2.25, within(PRECISION));
        assertThat(programming.status()).isEqualTo(ProfileStatus.ACADEMIC);
        assertThat(programming.confidence()).isCloseTo(2.25 / 4.25, within(PRECISION));

        // Problem Solving: IP (weight 0.6) and the failed MD with grade 8 (weight 0.5).
        assertThat(profile.scoreOf("PROB").orElseThrow().score())
                .isCloseTo((0.85 * 0.6 + 0.4 * 0.5) / 1.1, within(PRECISION));
    }

    @Test
    void competencyOnlyFromEnrolledCourseUnitsIsUnknown() {
        StudentProfile profile = calculator.calculate(COMPETENCIES, anasRecords(), LINKS, List.of());

        AggregatedScore algorithms = profile.scoreOf("ALGO").orElseThrow();
        assertThat(algorithms.status()).isEqualTo(ProfileStatus.UNKNOWN);
        assertThat(algorithms.score()).isNull();
    }

    @Test
    void everyCompetencyAppearsInNameOrder() {
        StudentProfile profile = calculator.calculate(COMPETENCIES, List.of(), LINKS, List.of());

        assertThat(profile.scores()).extracting(s -> s.competency().getName())
                .containsExactly("Algorithms", "Mathematical Reasoning", "Problem Solving", "Programming");
        assertThat(profile.scores()).allSatisfy(s -> assertThat(s.score().status()).isEqualTo(ProfileStatus.UNKNOWN));
    }

    @Test
    void profileChangesWhenANewGradeIsAdded() {
        List<AcademicRecord> records = new ArrayList<>(anasRecords());
        double before = calculator.calculate(COMPETENCIES, records, LINKS, List.of()).scoreOf("PROG").orElseThrow().score();

        records.set(3, new AcademicRecord(ANA, AED, "2026/2027", 1, RecordStatus.PASSED, 19));
        StudentProfile after = calculator.calculate(COMPETENCIES, records, LINKS, List.of());

        assertThat(after.scoreOf("PROG").orElseThrow().score()).isGreaterThan(before);
        assertThat(after.scoreOf("ALGO").orElseThrow().score()).isCloseTo(0.95, within(PRECISION));
    }

    @Test
    void coldStartStudentGetsTheTeachersEstimateUntilGradesArrive() {
        List<CompetencyAssessment> assessments = List.of(
                new CompetencyAssessment(BRUNO, PROG, 0.6, LocalDate.of(2026, 10, 9)));

        StudentProfile onlyEstimate = calculator.calculate(COMPETENCIES, List.of(), LINKS, assessments);
        assertThat(onlyEstimate.scoreOf("PROG").orElseThrow().score()).isCloseTo(0.6, within(PRECISION));
        assertThat(onlyEstimate.scoreOf("PROG").orElseThrow().status()).isEqualTo(ProfileStatus.INITIAL_ONLY);

        List<AcademicRecord> firstGrade = List.of(new AcademicRecord(BRUNO, IP, "2026/2027", 1, RecordStatus.PASSED, 16));
        AggregatedScore blended = calculator.calculate(COMPETENCIES, firstGrade, LINKS, assessments)
                .scoreOf("PROG").orElseThrow();
        assertThat(blended.status()).isEqualTo(ProfileStatus.BLENDED);
        assertThat(blended.score()).isCloseTo((0.6 + 0.8) / 2, within(PRECISION));
    }
}
