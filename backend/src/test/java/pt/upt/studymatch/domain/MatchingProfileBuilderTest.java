package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** How profiles, including cold-start ones, become inputs of the grouping algorithm. */
class MatchingProfileBuilderTest {

    private static final double PRECISION = 0.001;
    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final Competency PROG = new Competency("PROG", "Programming", null);
    private static final Competency DATA = new Competency("DATA", "Databases", null);
    private static final MatchingCriteria CRITERIA = new MatchingCriteria(List.of(
            new MatchingCriteria.Criterion("PROG", "Programming", 1.0),
            new MatchingCriteria.Criterion("DATA", "Databases", 0.5)));

    private final EvidenceAggregator aggregator = new EvidenceAggregator();
    private final MatchingProfileBuilder builder = new MatchingProfileBuilder();

    @Test
    void aScoreIsTrustedAsMuchAsItsConfidenceAndTheRestIsTheClassAverage() {
        // Total weight 2 gives confidence 0.5; the class average in Programming is (0.9 + 0.5) / 2 = 0.7.
        ProfiledStudent ana = student("Ana", grades(PROG, 0.9, 2.0), grades(DATA, 0.6, 2.0));
        ProfiledStudent rui = student("Rui", grades(PROG, 0.5, 2.0), grades(DATA, 0.6, 2.0));

        List<MatchingStudent> result = builder.build(List.of(ana, rui), CRITERIA);

        assertThat(result.get(0).score("PROG")).isCloseTo(0.5 * 0.9 + 0.5 * 0.7, within(PRECISION));
        assertThat(result.get(1).score("PROG")).isCloseTo(0.5 * 0.5 + 0.5 * 0.7, within(PRECISION));
        assertThat(result.get(0).lowEvidence()).isFalse();
    }

    @Test
    void anUnknownCompetencyCountsAsTheClassAverageNotAsZero() {
        ProfiledStudent ana = student("Ana", grades(PROG, 0.9, 2.0), grades(DATA, 0.4, 2.0));
        ProfiledStudent carla = student("Carla");

        MatchingStudent result = builder.build(List.of(ana, carla), CRITERIA).get(1);

        assertThat(result.score("PROG")).isCloseTo(0.9, within(PRECISION));
        assertThat(result.score("DATA")).isCloseTo(0.4, within(PRECISION));
        assertThat(result.lowEvidence()).isTrue();
    }

    @Test
    void aStudentWithOnlyATeacherEstimateIsLowEvidenceAndPlacedNearTheAverage() {
        ProfiledStudent ana = student("Ana", grades(PROG, 0.5, 4.0), grades(DATA, 0.5, 4.0));
        AggregatedScore estimate = aggregator.aggregate(List.of(new Evidence(EvidenceType.TEACHER_ASSESSMENT,
                "Teacher", 0.9, ColdStartPolicy.TEACHER_ASSESSMENT_WEIGHT)));
        ProfiledStudent bruno = new ProfiledStudent(new Student("53007", "Bruno", "b@upt.pt", LEI, 2026),
                new StudentProfile(List.of(new StudentProfile.CompetencyScore(PROG, estimate))));

        MatchingStudent result = builder.build(List.of(ana, bruno), CRITERIA).get(1);

        // Programming class average = (0.5 + 0.9) / 2 = 0.7, confidence of the estimate = 1/3.
        assertThat(result.score("PROG")).isCloseTo(0.9 / 3 + 0.7 * 2 / 3, within(PRECISION));
        assertThat(result.lowEvidence()).isTrue();
    }

    @Test
    void withNoEvidenceAtAllEveryoneIsNeutral() {
        List<MatchingStudent> result = builder.build(List.of(student("A"), student("B")), CRITERIA);

        assertThat(result).allSatisfy(s -> {
            assertThat(s.score("PROG")).isEqualTo(MatchingProfileBuilder.NEUTRAL_SCORE);
            assertThat(s.overall()).isCloseTo(MatchingProfileBuilder.NEUTRAL_SCORE, within(PRECISION));
        });
    }

    @Test
    void theOverallScoreWeighsCompetenciesByImportance() {
        ProfiledStudent ana = student("Ana", grades(PROG, 0.8, 1000.0), grades(DATA, 0.2, 1000.0));

        MatchingStudent result = builder.build(List.of(ana), CRITERIA).getFirst();

        assertThat(result.overall()).isCloseTo((0.8 + 0.5 * 0.2) / 1.5, within(0.01));
    }

    private StudentProfile.CompetencyScore grades(Competency competency, double value, double weight) {
        return new StudentProfile.CompetencyScore(competency,
                aggregator.aggregate(List.of(new Evidence(EvidenceType.ACADEMIC_RESULT, "Grades", value, weight))));
    }

    private static ProfiledStudent student(String name, StudentProfile.CompetencyScore... scores) {
        return new ProfiledStudent(new Student("5" + Math.abs(name.hashCode() % 10000), name, name + "@upt.pt", LEI, 2025),
                new StudentProfile(new ArrayList<>(List.of(scores))));
    }
}
