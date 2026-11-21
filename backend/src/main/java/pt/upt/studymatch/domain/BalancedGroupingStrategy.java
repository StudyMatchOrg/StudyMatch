package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * The team's matching strategy: balanced groups of complementary students.
 *
 * <ol>
 *   <li><b>Start:</b> students are ordered from strongest to weakest overall and dealt to the
 *       groups like cards, back and forth (1, 2, 3, 3, 2, 1, ...), so every group gets a fair mix.</li>
 *   <li><b>Improve:</b> repeatedly make the single swap of two students between groups that most
 *       improves the solution, until no swap helps. A solution is better when its weakest group is
 *       stronger ({@link GroupScorer#compareSolutions}). Because a group's strength only counts its
 *       best and second-best member per competency, swaps that bring complementary skills into a
 *       group are the ones that improve it.</li>
 * </ol>
 *
 * <p>Deterministic: the seed only breaks ties between students with the same overall score.
 */
public final class BalancedGroupingStrategy implements GroupingStrategy {

    public static final String CODE = "BALANCED";

    /** Safety limit; in practice the search stops much earlier because no swap improves. */
    static final int MAX_SWAPS = 1_000;

    private final GroupScorer scorer = new GroupScorer();

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public String description() {
        return "Balanced, complementary groups: makes the weakest group as strong as possible.";
    }

    @Override
    public List<List<MatchingStudent>> form(List<MatchingStudent> students, List<Integer> sizes,
            MatchingCriteria criteria, long seed) {
        List<List<MatchingStudent>> groups = deal(students, sizes, seed);
        improveBySwapping(groups, criteria);
        return groups;
    }

    private static List<List<MatchingStudent>> deal(List<MatchingStudent> students, List<Integer> sizes, long seed) {
        List<MatchingStudent> ordered = new ArrayList<>(students);
        Collections.shuffle(ordered, new Random(seed));
        ordered.sort(Comparator.comparingDouble(MatchingStudent::overall).reversed());

        List<List<MatchingStudent>> groups = new ArrayList<>();
        sizes.forEach(size -> groups.add(new ArrayList<>()));
        int largest = sizes.stream().mapToInt(Integer::intValue).max().orElse(0);
        int next = 0;
        for (int round = 0; round < largest; round++) {
            for (int i = 0; i < groups.size(); i++) {
                int group = round % 2 == 0 ? i : groups.size() - 1 - i;
                if (groups.get(group).size() < sizes.get(group)) {
                    groups.get(group).add(ordered.get(next++));
                }
            }
        }
        return groups;
    }

    private void improveBySwapping(List<List<MatchingStudent>> groups, MatchingCriteria criteria) {
        double[] scores = scorer.scores(groups, criteria);
        for (int swaps = 0; swaps < MAX_SWAPS; swaps++) {
            Swap best = findBestSwap(groups, criteria, scores);
            if (best == null) {
                return;
            }
            MatchingStudent first = groups.get(best.groupA()).get(best.memberA());
            groups.get(best.groupA()).set(best.memberA(), groups.get(best.groupB()).get(best.memberB()));
            groups.get(best.groupB()).set(best.memberB(), first);
            scores = best.scores();
        }
    }

    private Swap findBestSwap(List<List<MatchingStudent>> groups, MatchingCriteria criteria, double[] current) {
        Swap best = null;
        double[] bestScores = current;
        for (int a = 0; a < groups.size(); a++) {
            for (int b = a + 1; b < groups.size(); b++) {
                for (int i = 0; i < groups.get(a).size(); i++) {
                    for (int j = 0; j < groups.get(b).size(); j++) {
                        double[] candidate = scoresAfterSwap(groups, criteria, current, a, i, b, j);
                        if (GroupScorer.compareSolutions(candidate, bestScores) > 0) {
                            best = new Swap(a, i, b, j, candidate);
                            bestScores = candidate;
                        }
                    }
                }
            }
        }
        return best;
    }

    private double[] scoresAfterSwap(List<List<MatchingStudent>> groups, MatchingCriteria criteria, double[] current,
            int a, int i, int b, int j) {
        List<MatchingStudent> groupA = new ArrayList<>(groups.get(a));
        List<MatchingStudent> groupB = new ArrayList<>(groups.get(b));
        MatchingStudent fromA = groupA.get(i);
        groupA.set(i, groupB.get(j));
        groupB.set(j, fromA);
        double[] scores = current.clone();
        scores[a] = scorer.score(groupA, criteria);
        scores[b] = scorer.score(groupB, criteria);
        return scores;
    }

    private record Swap(int groupA, int memberA, int groupB, int memberB, double[] scores) {
    }
}
