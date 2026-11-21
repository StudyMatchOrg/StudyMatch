package pt.upt.studymatch.domain;

import java.util.List;

/**
 * A way of splitting students into groups. The rest of the application only depends on this
 * interface, so a strategy can be added or replaced (e.g. a genetic algorithm) without changing
 * anything else.
 */
public interface GroupingStrategy {

    /** Identifier used by the API, e.g. {@code BALANCED}. */
    String code();

    /** Short explanation shown to the teacher. */
    String description();

    /**
     * @param students the eligible students, each exactly once
     * @param sizes    the size of each group, from {@link GroupSizePlanner}
     * @param criteria the competencies that matter and their importance
     * @param seed     makes the result reproducible: the same input and seed give the same groups
     * @return one list of members per group, with exactly the given sizes
     */
    List<List<MatchingStudent>> form(List<MatchingStudent> students, List<Integer> sizes, MatchingCriteria criteria,
            long seed);
}
