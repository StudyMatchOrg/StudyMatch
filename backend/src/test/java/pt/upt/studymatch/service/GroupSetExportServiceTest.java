package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** How values are written in the exported CSV. */
class GroupSetExportServiceTest {

    @Test
    void plainValuesAreWrittenAsTheyAre() {
        assertThat(GroupSetExportService.cell("João Ribeiro")).isEqualTo("João Ribeiro");
    }

    @Test
    void valuesWithSeparatorsOrQuotesAreQuoted() {
        assertThat(GroupSetExportService.cell("Silva; Ana")).isEqualTo("\"Silva; Ana\"");
        assertThat(GroupSetExportService.cell("Ana \"Nini\" Silva")).isEqualTo("\"Ana \"\"Nini\"\" Silva\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"=HYPERLINK(\"http://evil\")", "+1+1", "-2", "@SUM(A1)"})
    void valuesThatLookLikeFormulasCannotRunInASpreadsheet(String value) {
        assertThat(GroupSetExportService.cell(value)).contains("'" + value.charAt(0));
    }
}
