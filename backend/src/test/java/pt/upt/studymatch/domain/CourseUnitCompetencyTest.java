package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.exception.BusinessRuleException;

class CourseUnitCompetencyTest {

    private static final CourseUnit POO = new CourseUnit("POO", "Programação Orientada a Objetos", 7.5, 1, 2,
            Set.of(new Degree("LEI", "Engenharia Informática")));
    private static final Competency PROGRAMMING = new Competency("PROG", "Programming", null);

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 0.5, 1.0})
    void weightsAboveZeroUpToOneAreAccepted(double weight) {
        assertThat(new CourseUnitCompetency(POO, PROGRAMMING, weight).getWeight()).isEqualTo(weight);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.5, 1.01})
    void weightsOutsideTheRangeAreRejected(double weight) {
        assertThatThrownBy(() -> new CourseUnitCompetency(POO, PROGRAMMING, weight))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("greater than 0 and at most 1");
    }
}
