package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotNull;

/** Links a course unit to a competency with a weight (greater than 0 and at most 1). */
public record CourseUnitCompetencyRequest(@NotNull Long courseUnitId, @NotNull Long competencyId,
        @NotNull Double weight) {
}
