package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.exception.BusinessRuleException;

class GroupSizePlannerTest {

    private final GroupSizePlanner planner = new GroupSizePlanner();

    @Test
    void twentyThreeStudentsInGroupsOfFiveGiveThreeFivesAndTwoFours() {
        GroupSizePlanner.GroupSizePlan plan = planner.plan(23, 5);

        assertThat(plan.sizes()).containsExactly(5, 5, 5, 4, 4);
        assertThat(plan.warnings()).isEmpty();
    }

    @ParameterizedTest(name = "{0} students, size {1} -> {2}")
    @CsvSource({
            "20, 5, 5 5 5 5",
            "12, 5, 4 4 4",
            "13, 5, 5 4 4",
            "9, 5, 5 4",
            "5, 5, 5",
            "4, 5, 4",
            "14, 3, 3 3 3 3 2",
            "10, 2, 2 2 2 2 2"})
    void sizesDifferByAtMostOneAndStayCloseToTheChosenSize(int students, int groupSize, String expected) {
        GroupSizePlanner.GroupSizePlan plan = planner.plan(students, groupSize);

        assertThat(plan.sizes()).containsExactlyElementsOf(sizes(expected));
        assertThat(plan.warnings()).isEmpty();
    }

    @ParameterizedTest(name = "{0} students, size {1} -> {2}")
    @CsvSource({
            "11, 5, 4 4 3",
            "7, 5, 4 3",
            "6, 5, 3 3",
            "3, 2, 3"})
    void impossibleSplitsStillFormGroupsButCarryAWarning(int students, int groupSize, String expected) {
        GroupSizePlanner.GroupSizePlan plan = planner.plan(students, groupSize);

        assertThat(plan.sizes()).containsExactlyElementsOf(sizes(expected));
        assertThat(plan.warnings()).singleElement().asString()
                .contains(students + " students cannot all be placed in groups of");
    }

    @Test
    void nobodyIsEverAloneInAGroup() {
        for (int students = 2; students <= 60; students++) {
            for (int size = GroupSizePlanner.MIN_GROUP_SIZE; size <= GroupSizePlanner.MAX_GROUP_SIZE; size++) {
                List<Integer> sizes = planner.plan(students, size).sizes();
                assertThat(sizes.stream().mapToInt(Integer::intValue).sum()).isEqualTo(students);
                assertThat(sizes).allMatch(s -> s >= 2);
                assertThat(sizes.getFirst() - sizes.getLast()).isLessThanOrEqualTo(1);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void fewerThanTwoStudentsCannotFormGroups(int students) {
        assertThatThrownBy(() -> planner.plan(students, 5))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("At least 2 eligible students");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 10})
    void groupSizesAtTheLimitsAreAccepted(int groupSize) {
        assertThat(planner.plan(20, groupSize).sizes()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 11})
    void groupSizesOutsideTwoToTenAreRejected(int groupSize) {
        assertThatThrownBy(() -> planner.plan(20, groupSize))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Group size must be between 2 and 10");
    }

    private static List<Integer> sizes(String expected) {
        return Arrays.stream(expected.split(" ")).map(Integer::valueOf).toList();
    }
}
