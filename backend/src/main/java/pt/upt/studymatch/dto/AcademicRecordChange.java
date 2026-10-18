package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.upt.studymatch.domain.RecordStatus;

/**
 * Changes to an existing attempt. The course unit cannot change: delete the record and add a
 * new one instead.
 */
public record AcademicRecordChange(
        @NotBlank String academicYear,
        @NotNull Integer semester,
        @NotNull RecordStatus status,
        Integer grade) {
}
