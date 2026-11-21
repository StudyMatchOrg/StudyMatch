package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static pt.upt.studymatch.domain.MatchingTestData.PROG_DATA_ALGO;
import static pt.upt.studymatch.domain.MatchingTestData.classOf;
import static pt.upt.studymatch.domain.MatchingTestData.ids;
import static pt.upt.studymatch.domain.MatchingTestData.student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class GroupingStrategyTest {

    private static final List<Integer> SIZES_23 = List.of(5, 5, 5, 4, 4);

    private final GroupScorer scorer = new GroupScorer();

    static Stream<Arguments> strategies() {
        return Stream.of(Arguments.of(new BalancedGroupingStrategy()), Arguments.of(new RandomGroupingStrategy()));
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void everyStudentIsInExactlyOneGroupOfThePlannedSize(GroupingStrategy strategy) {
        List<MatchingStudent> students = classOf(23, 7);

        List<List<MatchingStudent>> groups = strategy.form(students, SIZES_23, PROG_DATA_ALGO, 42);

        assertThat(groups).extracting(List::size).containsExactlyElementsOf(SIZES_23);
        List<Long> placed = groups.stream().flatMap(List::stream).map(MatchingStudent::studentId).toList();
        assertThat(placed).doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(students.stream().map(MatchingStudent::studentId).toList());
    }

    @ParameterizedTest
    @MethodSource("strategies")
    void theSameInputAndSeedGiveTheSameGroups(GroupingStrategy strategy) {
        List<MatchingStudent> students = classOf(23, 7);

        List<List<Long>> first = strategy.form(students, SIZES_23, PROG_DATA_ALGO, 42).stream()
                .map(MatchingTestData::ids).toList();
        List<List<Long>> second = strategy.form(students, SIZES_23, PROG_DATA_ALGO, 42).stream()
                .map(MatchingTestData::ids).toList();

        assertThat(second).isEqualTo(first);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 4, 5})
    void balancedGroupsHaveAStrongerWeakestGroupThanRandomGroups(long seed) {
        List<MatchingStudent> students = classOf(23, seed);

        double[] balanced = scorer.scores(new BalancedGroupingStrategy().form(students, SIZES_23, PROG_DATA_ALGO, seed),
                PROG_DATA_ALGO);
        double[] random = scorer.scores(new RandomGroupingStrategy().form(students, SIZES_23, PROG_DATA_ALGO, seed),
                PROG_DATA_ALGO);

        assertThat(min(balanced)).isGreaterThanOrEqualTo(min(random));
        assertThat(GroupScorer.compareSolutions(balanced, random)).isGreaterThanOrEqualTo(0);
    }

    @Test
    void complementaryStudentsAreSpreadAcrossGroups() {
        // Two strong programmers who are weak at databases, two database experts who are weak
        // programmers, and four average students: each group should get one of each expert.
        List<MatchingStudent> students = new ArrayList<>(List.of(
                student(1, "Prog 1", 0.95, 0.2, 0.5), student(2, "Prog 2", 0.95, 0.2, 0.5),
                student(3, "Data 1", 0.2, 0.95, 0.5), student(4, "Data 2", 0.2, 0.95, 0.5)));
        for (long id = 5; id <= 8; id++) {
            students.add(student(id, "Average " + id, 0.5, 0.5, 0.5));
        }

        List<List<MatchingStudent>> groups = new BalancedGroupingStrategy().form(students, List.of(4, 4),
                PROG_DATA_ALGO, 1);

        for (List<MatchingStudent> group : groups) {
            assertThat(ids(group)).containsAnyOf(1L, 2L).containsAnyOf(3L, 4L);
            assertThat(ids(group).stream().filter(id -> id <= 2).count()).isEqualTo(1);
            assertThat(ids(group).stream().filter(id -> id == 3 || id == 4).count()).isEqualTo(1);
        }
    }

    @Test
    void differentSeedsCanGiveDifferentRandomGroups() {
        List<MatchingStudent> students = classOf(23, 7);
        RandomGroupingStrategy random = new RandomGroupingStrategy();

        assertThat(random.form(students, SIZES_23, PROG_DATA_ALGO, 1).stream().map(MatchingTestData::ids).toList())
                .isNotEqualTo(random.form(students, SIZES_23, PROG_DATA_ALGO, 2).stream()
                        .map(MatchingTestData::ids).toList());
    }

    private static double min(double[] scores) {
        return Arrays.stream(scores).min().orElseThrow();
    }
}
