package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static pt.upt.studymatch.domain.MatchingTestData.PROG_DATA_ALGO;
import static pt.upt.studymatch.domain.MatchingTestData.classOf;
import static pt.upt.studymatch.domain.MatchingTestData.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pt.upt.studymatch.exception.BusinessRuleException;

/** The mandatory group rules (brief section 3.1) and the explanation of the result. */
class GroupFormationTest {

    private static final double PRECISION = 0.001;

    private final GroupFormation formation = new GroupFormation();

    @Test
    void twentyThreeStudentsFormFiveGroupsAndEveryoneIsPlacedOnce() {
        List<MatchingStudent> students = classOf(23, 3);

        GroupFormation.FormationResult result = formation.form(students, 5, PROG_DATA_ALGO,
                new BalancedGroupingStrategy(), 3);

        assertThat(result.groups()).extracting(g -> g.members().size()).containsExactly(5, 5, 5, 4, 4);
        assertThat(result.groups()).extracting(GroupFormation.FormedGroup::number).containsExactly(1, 2, 3, 4, 5);
        assertThat(result.groups().stream().flatMap(g -> g.members().stream()).map(MatchingStudent::studentId))
                .doesNotHaveDuplicates().hasSize(23);
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void theResultReportsBalanceAndTheComparisonWithRandomGroups() {
        GroupFormation.FormationResult result = formation.form(classOf(23, 3), 5, PROG_DATA_ALGO,
                new BalancedGroupingStrategy(), 3);

        double weakest = result.groups().stream().mapToDouble(GroupFormation.FormedGroup::score).min().orElseThrow();
        double strongest = result.groups().stream().mapToDouble(GroupFormation.FormedGroup::score).max().orElseThrow();
        assertThat(result.weakestScore()).isCloseTo(weakest, within(PRECISION));
        assertThat(result.spread()).isCloseTo(strongest - weakest, within(PRECISION));
        assertThat(result.weakestScore()).isGreaterThanOrEqualTo(result.randomWeakestScore());
    }

    @Test
    void aStudentSelectedTwiceIsRejected() {
        List<MatchingStudent> students = new ArrayList<>(classOf(6, 1));
        students.add(students.getFirst());

        assertThatThrownBy(() -> formation.form(students, 3, PROG_DATA_ALGO, new BalancedGroupingStrategy(), 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("is selected twice");
    }

    @Test
    void aStrategyThatBreaksTheRulesIsNeverAccepted() {
        GroupingStrategy losesAStudent = new GroupingStrategy() {
            @Override
            public String code() {
                return "BROKEN";
            }

            @Override
            public String description() {
                return "Forgets the last student";
            }

            @Override
            public List<List<MatchingStudent>> form(List<MatchingStudent> students, List<Integer> sizes,
                    MatchingCriteria criteria, long seed) {
                return List.of(new ArrayList<>(students.subList(0, students.size() - 1)));
            }
        };

        assertThatThrownBy(() -> formation.form(classOf(4, 1), 5, PROG_DATA_ALGO, losesAStudent, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly one group");
    }

    @Test
    void impossibleSizesStillFormGroupsWithAWarning() {
        GroupFormation.FormationResult result = formation.form(classOf(11, 1), 5, PROG_DATA_ALGO,
                new BalancedGroupingStrategy(), 1);

        assertThat(result.groups()).extracting(g -> g.members().size()).containsExactly(4, 4, 3);
        assertThat(result.warnings()).singleElement().asString().contains("11 students");
    }

    @Test
    void eachGroupExplainsItsStrengthsGapsAndWhoCarriesThem() {
        List<MatchingStudent> students = List.of(
                student(1, "Ana", 0.9, 0.3, 0.4), student(2, "Rui", 0.6, 0.35, 0.45),
                student(3, "Eva", 0.4, 0.2, 0.9), student(4, "Tó", 0.5, 0.3, 0.85));

        GroupFormation.FormationResult result = formation.form(students, 4, PROG_DATA_ALGO,
                new BalancedGroupingStrategy(), 1);
        GroupFormation.FormedGroup group = result.groups().getFirst();

        assertThat(group.strengths()).extracting(CompetencyStrength::name)
                .containsExactly("Programming", "Databases", "Algorithms");
        CompetencyStrength programming = group.strengths().getFirst();
        assertThat(programming.carriedBy()).isEqualTo("Ana");
        assertThat(programming.strength()).isCloseTo((0.9 + 0.5 * 0.6) / 1.5, within(PRECISION));
        assertThat(GroupExplanation.strengths(group.strengths())).containsExactly("Algorithms", "Programming");
        assertThat(GroupExplanation.gaps(group.strengths())).containsExactly("Databases");
    }

    @Test
    void lowEvidenceStudentsAreStillPlacedAndFlagged() {
        List<MatchingStudent> students = new ArrayList<>(classOf(8, 2));
        students.add(new MatchingStudent(99L, "53015", "Carla", Map.of("PROG", 0.6, "DATA", 0.6, "ALGO", 0.6), 0.6,
                true));

        GroupFormation.FormationResult result = formation.form(students, 3, PROG_DATA_ALGO,
                new BalancedGroupingStrategy(), 2);

        assertThat(result.groups().stream().flatMap(g -> g.members().stream()))
                .filteredOn(MatchingStudent::lowEvidence)
                .singleElement()
                .extracting(MatchingStudent::name).isEqualTo("Carla");
    }
}
