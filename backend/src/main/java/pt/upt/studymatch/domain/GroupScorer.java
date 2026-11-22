package pt.upt.studymatch.domain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * How good a group is. For each relevant competency the group's strength is
 *
 * <pre>
 *   strength = (best member + 0.5 × second-best member) / 1.5
 * </pre>
 *
 * <p>The best member counts most, because one person who masters a competency can lead that part
 * of the work. The second-best counts half, so the group does not depend on a single person.
 * Members below the second add nothing: a third strong programmer does not make the group better
 * at programming, which is why the algorithm prefers complementary members.
 *
 * <p>The group score is the importance-weighted average of its strengths. Solutions are compared
 * with {@link #compareSolutions}: the one whose weakest group is stronger is better.
 */
public final class GroupScorer {

    public static final double SECOND_BEST_WEIGHT = 0.5;

    private static final double EPSILON = 1e-9;

    public double strength(List<MatchingStudent> members, String competencyCode) {
        List<Double> scores = members.stream()
                .map(m -> m.score(competencyCode))
                .sorted(Comparator.reverseOrder())
                .toList();
        if (scores.isEmpty()) {
            return 0;
        }
        if (scores.size() == 1) {
            return scores.getFirst();
        }
        return (scores.get(0) + SECOND_BEST_WEIGHT * scores.get(1)) / (1 + SECOND_BEST_WEIGHT);
    }

    public double score(List<MatchingStudent> members, MatchingCriteria criteria) {
        double weighted = 0;
        for (MatchingCriteria.Criterion criterion : criteria.criteria()) {
            weighted += criterion.importance() * strength(members, criterion.code());
        }
        return weighted / criteria.totalImportance();
    }

    public double[] scores(List<List<MatchingStudent>> groups, MatchingCriteria criteria) {
        return groups.stream().mapToDouble(group -> score(group, criteria)).toArray();
    }

    /**
     * Compares two solutions by their group scores, weakest first (leximin): the solution with
     * the stronger weakest group wins; if equal, the second weakest decides, and so on.
     *
     * @return a positive number if {@code a} is better, negative if {@code b} is better, 0 if equal
     */
    public static int compareSolutions(double[] a, double[] b) {
        double[] sortedA = a.clone();
        double[] sortedB = b.clone();
        Arrays.sort(sortedA);
        Arrays.sort(sortedB);
        for (int i = 0; i < Math.min(sortedA.length, sortedB.length); i++) {
            double difference = sortedA[i] - sortedB[i];
            if (Math.abs(difference) > EPSILON) {
                return difference > 0 ? 1 : -1;
            }
        }
        return 0;
    }
}
