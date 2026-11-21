package pt.upt.studymatch.domain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.junit.jupiter.api.Test;

/**
 * The single evidence rule used for every competency, and the cold-start behaviour that
 * follows from it.
 */
class EvidenceAggregatorTest {

    private static final double PRECISION = 0.001;

    private final EvidenceAggregator aggregator = new EvidenceAggregator();

    private static Evidence grade(String label, double value, double weight) {
        return new Evidence(EvidenceType.ACADEMIC_RESULT, label, value, weight);
    }

    private static Evidence teacher(double value) {
        return new Evidence(EvidenceType.TEACHER_ASSESSMENT, "Teacher", value, ColdStartPolicy.TEACHER_ASSESSMENT_WEIGHT);
    }

    private static double teacherShare(AggregatedScore score) {
        return score.evidence().stream()
                .filter(c -> c.evidence().type() == EvidenceType.TEACHER_ASSESSMENT)
                .mapToDouble(AggregatedScore.Contribution::share)
                .sum();
    }

    @Test
    void noEvidenceMeansUnknownNotZero() {
        AggregatedScore score = aggregator.aggregate(List.of());

        assertThat(score.isKnown()).isFalse();
        assertThat(score.score()).isNull();
        assertThat(score.confidence()).isZero();
        assertThat(score.status()).isEqualTo(ProfileStatus.UNKNOWN);
    }

    @Test
    void onlyATeacherAssessmentGivesItsValueWithLowConfidence() {
        AggregatedScore score = aggregator.aggregate(List.of(teacher(0.6)));

        assertThat(score.score()).isCloseTo(0.6, within(PRECISION));
        assertThat(score.status()).isEqualTo(ProfileStatus.INITIAL_ONLY);
        assertThat(score.confidence()).isCloseTo(1.0 / 3, within(PRECISION));
    }

    @Test
    void teacherAssessmentCountsLessAsGradesArrive() {
        Evidence ip = grade("IP", 0.6, 1.0);
        Evidence poo = grade("POO", 0.6, 1.0);
        Evidence bd = grade("BD", 0.6, 1.0);

        assertThat(teacherShare(aggregator.aggregate(List.of(teacher(0.9))))).isCloseTo(1.0, within(PRECISION));
        assertThat(teacherShare(aggregator.aggregate(List.of(teacher(0.9), ip)))).isCloseTo(0.5, within(PRECISION));
        assertThat(teacherShare(aggregator.aggregate(List.of(teacher(0.9), ip, poo, bd))))
                .isCloseTo(0.25, within(PRECISION));
    }

    @Test
    void scoreMovesFromTheTeacherEstimateTowardsTheGrades() {
        AggregatedScore before = aggregator.aggregate(List.of(teacher(0.9)));
        AggregatedScore after = aggregator.aggregate(List.of(teacher(0.9), grade("IP", 0.5, 1.0)));

        assertThat(before.score()).isCloseTo(0.9, within(PRECISION));
        assertThat(after.score()).isCloseTo(0.7, within(PRECISION));
        assertThat(after.status()).isEqualTo(ProfileStatus.BLENDED);
        assertThat(after.confidence()).isGreaterThan(before.confidence());
    }

    @Test
    void workedExampleFromTheDesignDocument() {
        // Programming: POO (weight 1.0 × 7.5/6 ECTS, grade 17), IP (weight 0.8 × 6/6 ECTS, grade 15)
        // and a teacher assessment of 0.5.
        AggregatedScore score = aggregator.aggregate(List.of(
                grade("POO", 17 / 20.0, 1.25), grade("IP", 15 / 20.0, 0.8), teacher(0.5)));

        assertThat(score.score()).isCloseTo(0.709, within(PRECISION));
        assertThat(score.confidence()).isCloseTo(0.604, within(PRECISION));
        assertThat(score.totalWeight()).isCloseTo(3.05, within(PRECISION));
        assertThat(score.evidence()).extracting(c -> c.evidence().label()).containsExactly("POO", "Teacher", "IP");
        assertThat(score.evidence()).extracting(AggregatedScore.Contribution::share)
                .containsExactly(1.25 / 3.05, 1.0 / 3.05, 0.8 / 3.05);
    }

    @Test
    void onlyGradesGiveAnAcademicScore() {
        AggregatedScore score = aggregator.aggregate(List.of(grade("BD", 17 / 20.0, 1.0)));

        assertThat(score.status()).isEqualTo(ProfileStatus.ACADEMIC);
        assertThat(score.score()).isCloseTo(0.85, within(PRECISION));
    }

    @Test
    void confidenceIsOneHalfWhenTheTotalWeightEqualsTheConstant() {
        assertThat(EvidenceAggregator.confidence(0)).isZero();
        assertThat(EvidenceAggregator.confidence(EvidenceAggregator.CONFIDENCE_CONSTANT)).isCloseTo(0.5, within(PRECISION));
        assertThat(EvidenceAggregator.confidence(100)).isLessThan(1.0);
    }
}
