package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import pt.upt.studymatch.exception.BusinessRuleException;

class CourseUnitTest {

    private static final Degree LEI = new Degree("LEI", "Engenharia Informática");
    private static final Degree LIG = new Degree("LIG", "Informática de Gestão");

    @Test
    void courseUnitBelongsOnlyToItsDegrees() {
        CourseUnit databases = new CourseUnit("BD", "Bases de Dados", 6, 2, 1, Set.of(LEI));

        assertThat(databases.belongsTo(LEI)).isTrue();
        assertThat(databases.belongsTo(LIG)).isFalse();
    }

    @Test
    void courseUnitMustBelongToAtLeastOneDegree() {
        assertThatThrownBy(() -> new CourseUnit("BD", "Bases de Dados", 6, 2, 1, Set.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at least one degree");
    }

    @Test
    void ectsMustBePositive() {
        assertThatThrownBy(() -> new CourseUnit("BD", "Bases de Dados", 0, 2, 1, Set.of(LEI)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ECTS");
    }

    @Test
    void semesterMustBeOneOrTwo() {
        assertThatThrownBy(() -> new CourseUnit("BD", "Bases de Dados", 6, 2, 3, Set.of(LEI)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Semester");
    }
}
