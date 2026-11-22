package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * Decides how many groups to form and how big each one is. The teacher chooses the group size
 * for each project; the sizes then vary as little as possible:
 *
 * <pre>
 *   number of groups = ⌈students / size⌉     (never more than students / 2, so no one is alone)
 *   sizes            = as equal as possible, differing by at most 1, largest first
 * </pre>
 *
 * <p>With size 5: 23 students give 5+5+5+4+4 and 12 give 4+4+4, so groups have 4 or 5 students
 * whenever that is mathematically possible. When it is not (e.g. 11 give 4+4+3), the plan
 * carries a warning that the application shows and saves with the groups.
 */
public final class GroupSizePlanner {

    public static final int MIN_GROUP_SIZE = 2;
    public static final int MAX_GROUP_SIZE = 10;
    public static final int MIN_STUDENTS = 2;

    public GroupSizePlan plan(int students, int groupSize) {
        if (groupSize < MIN_GROUP_SIZE || groupSize > MAX_GROUP_SIZE) {
            throw new BusinessRuleException("Group size must be between " + MIN_GROUP_SIZE + " and "
                    + MAX_GROUP_SIZE + ", but was " + groupSize);
        }
        if (students < MIN_STUDENTS) {
            throw new BusinessRuleException("At least " + MIN_STUDENTS
                    + " eligible students are needed to form groups, but there are " + students);
        }
        int groups = Math.min(ceilDiv(students, groupSize), students / MIN_GROUP_SIZE);
        int base = students / groups;
        int larger = students % groups;
        List<Integer> sizes = new ArrayList<>();
        for (int i = 0; i < groups; i++) {
            sizes.add(i < larger ? base + 1 : base);
        }
        return new GroupSizePlan(sizes, warnings(students, groupSize, sizes));
    }

    private static List<String> warnings(int students, int groupSize, List<Integer> sizes) {
        boolean allClose = sizes.stream().allMatch(size -> size == groupSize || size == groupSize - 1);
        if (allClose) {
            return List.of();
        }
        String described = sizes.stream().map(String::valueOf).collect(Collectors.joining(" + "));
        return List.of(students + " students cannot all be placed in groups of " + (groupSize - 1) + " or "
                + groupSize + ", so the groups are " + described + ".");
    }

    private static int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }

    /**
     * @param sizes    the size of each group, largest first
     * @param warnings why some groups differ from the chosen size, if they do
     */
    public record GroupSizePlan(List<Integer> sizes, List<String> warnings) {

        public int groups() {
            return sizes.size();
        }
    }
}
