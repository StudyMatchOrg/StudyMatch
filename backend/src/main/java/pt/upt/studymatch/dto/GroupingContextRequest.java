package pt.upt.studymatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * A project as the teacher defines it. The course unit and academic year are optional; they are
 * used to find the enrolled students, but any students can be chosen.
 */
public record GroupingContextRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        Long courseUnitId,
        @Size(max = 9) String academicYear,
        @NotNull Integer groupSize,
        @NotNull List<@NotNull Long> studentIds,
        @NotNull List<@Valid @NotNull CompetencyImportance> competencies) {

    public record CompetencyImportance(@NotNull Long competencyId, @NotNull Double importance) {
    }
}
