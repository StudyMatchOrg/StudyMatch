package pt.upt.studymatch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import pt.upt.studymatch.domain.RecordStatus;
import pt.upt.studymatch.exception.BusinessRuleException;

/**
 * Reads academic records from CSV text. The first line is a header with the column names, in any
 * order; columns are separated by commas or by semicolons (as exported by Excel in Portugal).
 *
 * <pre>
 * studentNumber,courseUnitCode,academicYear,semester,status,grade
 * 52031,IP,2025/2026,1,PASSED,17
 * 52031,AED,2026/2027,1,ENROLLED,
 * </pre>
 *
 * <p>Problems with the file as a whole (empty, missing columns) throw {@link BusinessRuleException}.
 * Problems with a single line are reported in {@link Result#errors()} so the other lines can still
 * be imported. Business rules (grade range, duplicates, ...) are checked later, when each line is
 * recorded.
 */
public final class AcademicRecordCsvParser {

    public static final List<String> COLUMNS =
            List.of("studentNumber", "courseUnitCode", "academicYear", "semester", "status", "grade");

    /** A line that could be read. {@code line} is the line number in the file, counting the header as 1. */
    public record Row(int line, String studentNumber, String courseUnitCode, String academicYear, int semester,
            RecordStatus status, Integer grade) {
    }

    public record LineError(int line, String message) {
    }

    public record Result(List<Row> rows, List<LineError> errors) {
    }

    public Result parse(String content) {
        String text = content == null ? "" : content.replace("﻿", "");
        String[] lines = text.split("\\r?\\n", -1);
        if (text.isBlank()) {
            throw new BusinessRuleException("The file is empty");
        }
        String separator = lines[0].contains(";") ? ";" : ",";
        Map<String, Integer> columns = columnIndexes(lines[0], separator);

        List<Row> rows = new ArrayList<>();
        List<LineError> errors = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            int lineNumber = i + 1;
            try {
                rows.add(parseRow(lineNumber, lines[i].split(separator, -1), columns));
            } catch (IllegalArgumentException ex) {
                errors.add(new LineError(lineNumber, ex.getMessage()));
            }
        }
        if (rows.isEmpty() && errors.isEmpty()) {
            throw new BusinessRuleException("The file has a header but no records");
        }
        return new Result(rows, errors);
    }

    private static Map<String, Integer> columnIndexes(String header, String separator) {
        Map<String, Integer> found = new HashMap<>();
        String[] names = header.split(separator, -1);
        for (int i = 0; i < names.length; i++) {
            found.put(names[i].trim().toLowerCase(Locale.ROOT), i);
        }
        Map<String, Integer> indexes = new HashMap<>();
        List<String> missing = new ArrayList<>();
        for (String column : COLUMNS) {
            Integer index = found.get(column.toLowerCase(Locale.ROOT));
            if (index == null) {
                missing.add(column);
            } else {
                indexes.put(column, index);
            }
        }
        if (!missing.isEmpty()) {
            throw new BusinessRuleException("Missing columns: " + String.join(", ", missing)
                    + ". The header must contain " + String.join(", ", COLUMNS));
        }
        return indexes;
    }

    private static Row parseRow(int line, String[] cells, Map<String, Integer> columns) {
        int needed = columns.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        if (cells.length < needed) {
            throw new IllegalArgumentException("Expected " + COLUMNS.size() + " columns but found " + cells.length);
        }
        String studentNumber = required(cells, columns, "studentNumber");
        String courseUnitCode = required(cells, columns, "courseUnitCode").toUpperCase(Locale.ROOT);
        String academicYear = required(cells, columns, "academicYear");
        int semester = integer(required(cells, columns, "semester"), "semester");
        RecordStatus status = status(required(cells, columns, "status"));
        String gradeText = cell(cells, columns, "grade");
        Integer grade = gradeText.isEmpty() ? null : integer(gradeText, "grade");
        return new Row(line, studentNumber, courseUnitCode, academicYear, semester, status, grade);
    }

    private static String cell(String[] cells, Map<String, Integer> columns, String column) {
        return cells[columns.get(column)].trim();
    }

    private static String required(String[] cells, Map<String, Integer> columns, String column) {
        String value = cell(cells, columns, column);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing " + column);
        }
        return value;
    }

    private static int integer(String value, String column) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + column + " '" + value + "': expected a whole number");
        }
    }

    private static RecordStatus status(String value) {
        try {
            return RecordStatus.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown status '" + value + "': use PASSED, FAILED or ENROLLED");
        }
    }
}
