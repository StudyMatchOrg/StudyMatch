package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Random groups, which is what teachers often do without a tool. Used as the baseline that the
 * balanced strategy is compared against, and available to the teacher as an option.
 */
public final class RandomGroupingStrategy implements GroupingStrategy {

    public static final String CODE = "RANDOM";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public String description() {
        return "Random groups, ignoring the profiles. Useful as a comparison.";
    }

    @Override
    public List<List<MatchingStudent>> form(List<MatchingStudent> students, List<Integer> sizes,
            MatchingCriteria criteria, long seed) {
        List<MatchingStudent> shuffled = new ArrayList<>(students);
        Collections.shuffle(shuffled, new Random(seed));
        List<List<MatchingStudent>> groups = new ArrayList<>();
        int next = 0;
        for (int size : sizes) {
            groups.add(new ArrayList<>(shuffled.subList(next, next + size)));
            next += size;
        }
        return groups;
    }
}
