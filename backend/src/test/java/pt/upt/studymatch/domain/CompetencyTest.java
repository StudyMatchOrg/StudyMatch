package pt.upt.studymatch.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.exception.BusinessRuleException;

class CompetencyTest {

    @ParameterizedTest
    @ValueSource(strings = {"P", "prog", "PRO G", "A_VERY_LONG_COMPETENCY_CODE"})
    void codesAreTwoToTwentyCapitalLettersDigitsOrUnderscores(String code) {
        assertThatThrownBy(() -> new Competency(code, "Name", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Competency code must have 2 to 20 characters");
    }

    @Test
    void aCompetencyNeedsAName() {
        assertThatThrownBy(() -> new Competency("PROG", " ", null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A competency needs a name");
    }

    @Test
    void nameAndDescriptionCanChangeButNotTheCode() {
        Competency competency = new Competency("QA_2", "Quality", "Old text");

        competency.change(" Software Quality ", " ");

        assertThat(competency.getCode()).isEqualTo("QA_2");
        assertThat(competency.getName()).isEqualTo("Software Quality");
        assertThat(competency.getDescription()).isNull();
    }
}
