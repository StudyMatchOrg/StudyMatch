package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.exception.BusinessRuleException;

class GroupingContextTest {

    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final Competency PROG = new Competency("PROG", "Programming", null);
    private static final Competency MATH = new Competency("MATH", "Mathematical Reasoning", null);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 20, 10, 0);

    private final UserAccount marta = account(1L, "marta", Role.TEACHER);

    @Test
    void aProjectNeedsAName() {
        assertThatThrownBy(() -> project(" ", 5))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A project needs a name");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 11})
    void groupSizeMustBeBetweenTwoAndTen(int groupSize) {
        assertThatThrownBy(() -> project("AED project", groupSize))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Group size must be between 2 and 10");
    }

    @Test
    void theAcademicYearFollowsTheSameRuleAsAcademicRecords() {
        assertThatThrownBy(() -> new GroupingContext("AED project", null, null, "2026/2028", 5, marta, NOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Academic year must look like 2025/2026");
    }

    @Test
    void aStudentCannotBeSelectedTwice() {
        GroupingContext project = project("AED project", 5);
        Student ana = student(1L, "52031", "Ana Sousa");

        assertThatThrownBy(() -> project.replaceStudents(List.of(ana, student(2L, "53007", "Bruno Costa"), ana)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Student 52031 is selected twice");
    }

    @Test
    void studentsAreListedByName() {
        GroupingContext project = project("AED project", 5);
        project.replaceStudents(List.of(student(1L, "53007", "Bruno Costa"), student(2L, "52031", "Ana Sousa")));

        assertThat(project.getStudents()).extracting(Student::getName).containsExactly("Ana Sousa", "Bruno Costa");
    }

    @Test
    void theRelevantCompetenciesBecomeTheMatchingCriteriaMostImportantFirst() {
        GroupingContext project = project("AED project", 5);
        Map<Competency, Double> importance = new LinkedHashMap<>();
        importance.put(MATH, 0.6);
        importance.put(PROG, 1.0);
        project.replaceCompetencies(importance);

        assertThat(project.criteria().criteria()).extracting(MatchingCriteria.Criterion::code)
                .containsExactly("PROG", "MATH");
    }

    @Test
    void atLeastOneCompetencyMustMatter() {
        assertThatThrownBy(() -> project("AED project", 5).replaceCompetencies(Map.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("At least one competency");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.01})
    void importanceMustBeGreaterThanZeroAndAtMostOne(double value) {
        assertThatThrownBy(() -> project("AED project", 5).replaceCompetencies(Map.of(PROG, value)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("greater than 0 and at most 1");
    }

    @Test
    void onlyTheOwnerAndAdminsCanSeeAProject() {
        GroupingContext project = project("AED project", 5);

        assertThat(project.isVisibleTo(marta)).isTrue();
        assertThat(project.isVisibleTo(account(2L, "rui", Role.TEACHER))).isFalse();
        assertThat(project.isVisibleTo(account(3L, "admin", Role.ADMIN))).isTrue();
    }

    private GroupingContext project(String name, int groupSize) {
        return new GroupingContext(name, null, null, "2026/2027", groupSize, marta, NOW);
    }

    private static Student student(Long id, String number, String name) {
        return withId(new Student(number, name, number + "@alunos.upt.pt", LEI, 2025), id);
    }

    private static UserAccount account(Long id, String username, Role role) {
        return withId(new UserAccount(username, username, "hash", role), id);
    }

    // Entities only get an id when saved; these unit tests set it directly.
    private static <T> T withId(T entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
