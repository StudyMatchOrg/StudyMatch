package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * Forms the groups of one generation and explains them. It enforces the mandatory rules for any
 * strategy: every eligible student is considered, nobody appears twice, and every student ends
 * up in exactly one group of the planned size.
 *
 * <p>Plain Java with no Spring or database dependency, like the profile calculation.
 */
public final class GroupFormation {

    private final GroupSizePlanner planner = new GroupSizePlanner();
    private final GroupScorer scorer = new GroupScorer();
    private final GroupingStrategy baseline = new RandomGroupingStrategy();

    public FormationResult form(List<MatchingStudent> students, int groupSize, MatchingCriteria criteria,
            GroupingStrategy strategy, long seed) {
        rejectDuplicates(students);
        GroupSizePlanner.GroupSizePlan plan = planner.plan(students.size(), groupSize);
        List<List<MatchingStudent>> groups = strategy.form(students, plan.sizes(), criteria, seed);
        checkMandatoryRules(students, plan.sizes(), groups, strategy);

        double[] scores = scorer.scores(groups, criteria);
        double[] randomScores = scorer.scores(baseline.form(students, plan.sizes(), criteria, seed), criteria);
        List<FormedGroup> formed = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            formed.add(explain(i + 1, groups.get(i), scores[i], criteria));
        }
        double weakest = Arrays.stream(scores).min().orElse(0);
        double strongest = Arrays.stream(scores).max().orElse(0);
        return new FormationResult(formed, weakest, Arrays.stream(scores).average().orElse(0), strongest - weakest,
                Arrays.stream(randomScores).min().orElse(0), plan.warnings());
    }

    private static void rejectDuplicates(List<MatchingStudent> students) {
        Set<Long> seen = new HashSet<>();
        for (MatchingStudent student : students) {
            if (!seen.add(student.studentId())) {
                throw new BusinessRuleException("Student " + student.studentNumber() + " is selected twice");
            }
        }
    }

    private static void checkMandatoryRules(List<MatchingStudent> students, List<Integer> sizes,
            List<List<MatchingStudent>> groups, GroupingStrategy strategy) {
        List<Integer> actualSizes = groups.stream().map(List::size).toList();
        Set<Long> placed = new HashSet<>();
        groups.forEach(group -> group.forEach(member -> placed.add(member.studentId())));
        boolean everyoneOnce = placed.size() == students.size()
                && actualSizes.stream().mapToInt(Integer::intValue).sum() == students.size()
                && students.stream().allMatch(s -> placed.contains(s.studentId()));
        if (!everyoneOnce || !actualSizes.equals(sizes)) {
            throw new IllegalStateException("Strategy " + strategy.code()
                    + " broke the group rules: every student must be in exactly one group of the planned size");
        }
    }

    private FormedGroup explain(int number, List<MatchingStudent> members, double score, MatchingCriteria criteria) {
        List<CompetencyStrength> strengths = criteria.criteria().stream()
                .map(c -> new CompetencyStrength(c.code(), c.name(), c.importance(),
                        scorer.strength(members, c.code()), carrier(members, c.code())))
                .toList();
        List<MatchingStudent> ordered = members.stream()
                .sorted(Comparator.comparingDouble(MatchingStudent::overall).reversed())
                .toList();
        return new FormedGroup(number, ordered, score, strengths);
    }

    private static String carrier(List<MatchingStudent> members, String competencyCode) {
        return members.stream()
                .max(Comparator.comparingDouble(m -> m.score(competencyCode)))
                .map(MatchingStudent::name)
                .orElse(null);
    }

    /**
     * One generation's result.
     *
     * @param weakestScore       score of the weakest group, which the balanced strategy maximises
     * @param spread             strongest minus weakest group score: smaller is more balanced
     * @param randomWeakestScore weakest group of random groups for the same students and seed,
     *                           to show what the strategy gained
     * @param warnings           e.g. why some groups are smaller than the chosen size
     */
    public record FormationResult(List<FormedGroup> groups, double weakestScore, double averageScore, double spread,
            double randomWeakestScore, List<String> warnings) {
    }

    /** A group with its members (strongest overall first), its score and its competency strengths. */
    public record FormedGroup(int number, List<MatchingStudent> members, double score,
            List<CompetencyStrength> strengths) {
    }
}
