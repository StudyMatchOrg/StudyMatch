package pt.upt.studymatch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.exception.BusinessRuleException;
import pt.upt.studymatch.service.AcademicRecordCsvParser.Result;
import pt.upt.studymatch.service.AcademicRecordCsvParser.Row;

class AcademicRecordCsvParserTest {

    private static final String HEADER = "studentNumber,courseUnitCode,academicYear,semester,status,grade\n";

    private final AcademicRecordCsvParser parser = new AcademicRecordCsvParser();

    @Test
    void validFileIsRead() {
        Result result = parser.parse(HEADER + "52031,IP,2025/2026,1,PASSED,17\n52031,aed,2026/2027,1,enrolled,\n");

        assertThat(result.errors()).isEmpty();
        assertThat(result.rows()).containsExactly(
                new Row(2, "52031", "IP", "2025/2026", 1, RecordStatus.PASSED, 17),
                new Row(3, "52031", "AED", "2026/2027", 1, RecordStatus.ENROLLED, null));
    }

    @Test
    void semicolonsAnyColumnOrderBlankLinesAndExcelBomAreAccepted() {
        String content = "﻿grade;status;semester;academicYear;courseUnitCode;studentNumber\r\n"
                + "\r\n"
                + "15;PASSED;2;2025/2026;POO;52031\r\n";

        Result result = parser.parse(content);

        assertThat(result.rows()).containsExactly(new Row(3, "52031", "POO", "2025/2026", 2, RecordStatus.PASSED, 15));
    }

    @Test
    void invalidLinesAreReportedWithTheirLineNumber() {
        Result result = parser.parse(HEADER
                + "52031,IP,2025/2026,one,PASSED,17\n"
                + "52031,IP,2025/2026,1,APROVADO,17\n"
                + "52031,IP,2025/2026,1,PASSED,dezassete\n"
                + "52031,IP\n"
                + "52031,POO,2025/2026,2,PASSED,15\n");

        assertThat(result.rows()).hasSize(1);
        assertThat(result.errors()).extracting(AcademicRecordCsvParser.LineError::line).containsExactly(2, 3, 4, 5);
        assertThat(result.errors().get(0).message()).contains("semester");
        assertThat(result.errors().get(1).message()).contains("Unknown status 'APROVADO'");
        assertThat(result.errors().get(2).message()).contains("grade");
        assertThat(result.errors().get(3).message()).contains("Expected 6 columns");
    }

    @Test
    void emptyFileIsRejected() {
        assertThatThrownBy(() -> parser.parse("  \n"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The file is empty");
    }

    @Test
    void headerWithoutRecordsIsRejected() {
        assertThatThrownBy(() -> parser.parse(HEADER))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The file has a header but no records");
    }

    @Test
    void missingColumnsAreNamed() {
        assertThatThrownBy(() -> parser.parse("studentNumber,courseUnitCode,grade\n52031,IP,17\n"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageStartingWith("Missing columns: academicYear, semester, status");
    }
}
