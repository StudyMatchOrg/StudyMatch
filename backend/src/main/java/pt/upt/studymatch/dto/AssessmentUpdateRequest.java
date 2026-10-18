package pt.upt.studymatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * The complete set of initial assessments of a student. Competencies that are left out, or
 * sent with a {@code null} value, are no longer assessed.
 */
public record AssessmentUpdateRequest(@NotNull List<@Valid Entry> assessments) {

    public record Entry(@NotNull Long competencyId, Double value) {
    }
}
