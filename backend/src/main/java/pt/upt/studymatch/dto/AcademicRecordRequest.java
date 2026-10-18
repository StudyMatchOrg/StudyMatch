package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.upt.studymatch.domain.RecordStatus;

/**
 * A new attempt at a course unit. The grade rules (0–20, coherent with the status) are checked
 * by {@link pt.upt.studymatch.domain.AcademicRecord}, so they apply everywhere records are created.
 */
public record AcademicRecordRequest(
        @NotNull Long courseUnitId,
        @NotBlank String academicYear,
        @NotNull Integer semester,
        @NotNull RecordStatus status,
        Integer grade) {
}
