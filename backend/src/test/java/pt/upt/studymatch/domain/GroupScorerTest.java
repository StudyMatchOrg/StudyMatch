package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static pt.upt.studymatch.domain.MatchingTestData.student;

import java.util.List;
import org.junit.jupiter.api.Test;

class GroupScorerTest {

    private static final double PRECISION = 0.001;

    private final GroupScorer scorer = new GroupScorer();

    @Test
    void strengthIsTheBestMemberPlusHalfTheSecondBest() {
        List<MatchingStudent> group = List.of(
                student(1, "A", 0.9, 0, 0), student(2, "B", 0.6, 0, 0), student(3, "C", 0.3, 0, 0));

        assertThat(scorer.strength(group, "PROG")).isCloseTo((0.9 + 0.5 * 0.6) / 1.5, within(PRECISION));
    }

    @Test
    void aThirdStrongMemberDoesNotMakeTheGroupStronger() {
        List<MatchingStudent> two = List.of(student(1, "A", 0.9, 0, 0), student(2, "B", 0.8, 0, 0));
        List<MatchingStudent> three = List.of(student(1, "A", 0.9, 0, 0), student(2, "B", 0.8, 0, 0),
                student(3, "C", 0.85, 0, 0));
        List<MatchingStudent> withWeakThird = List.of(student(1, "A", 0.9, 0, 0), student(2, "B", 0.8, 0, 0),
                student(3, "C", 0.1, 0, 0));

        assertThat(scorer.strength(withWeakThird, "PROG")).isCloseTo(scorer.strength(two, "PROG"), within(PRECISION));
        assertThat(scorer.strength(three, "PROG")).isCloseTo((0.9 + 0.5 * 0.85) / 1.5, within(PRECISION));
    }

    @Test
    void aOneMemberGroupIsAsStrongAsItsMember() {
        assertThat(scorer.strength(List.of(student(1, "A", 0.7, 0, 0)), "PROG")).isCloseTo(0.7, within(PRECISION));
    }

    @Test
    void groupScoreWeighsCompetenciesByImportance() {
        MatchingCriteria criteria = new MatchingCriteria(List.of(
                new MatchingCriteria.Criterion("PROG", "Programming", 1.0),
                new MatchingCriteria.Criterion("DATA", "Databases", 0.5)));
        List<MatchingStudent> group = List.of(student(1, "A", 0.9, 0.3, 0), student(2, "B", 0.5, 0.5, 0));

        double programming = (0.9 + 0.5 * 0.5) / 1.5;
        double databases = (0.5 + 0.5 * 0.3) / 1.5;
        assertThat(scorer.score(group, criteria))
                .isCloseTo((programming + 0.5 * databases) / 1.5, within(PRECISION));
    }

    @Test
    void theSolutionWithTheStrongerWeakestGroupIsBetter() {
        assertThat(GroupScorer.compareSolutions(new double[] {0.7, 0.7}, new double[] {0.9, 0.6})).isPositive();
        assertThat(GroupScorer.compareSolutions(new double[] {0.9, 0.6}, new double[] {0.7, 0.7})).isNegative();
    }

    @Test
    void whenTheWeakestGroupsAreEqualTheNextWeakestDecides() {
        assertThat(GroupScorer.compareSolutions(new double[] {0.6, 0.8, 0.7}, new double[] {0.7, 0.6, 0.7}))
                .isPositive();
        assertThat(GroupScorer.compareSolutions(new double[] {0.6, 0.7}, new double[] {0.7, 0.6})).isZero();
    }
}
