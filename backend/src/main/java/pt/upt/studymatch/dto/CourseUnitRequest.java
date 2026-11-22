package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** A new course unit. ECTS, curricular year, semester and degrees are checked by the entity. */
public record CourseUnitRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull Double ects,
        @NotNull Integer curricularYear,
        @NotNull Integer semester,
        @NotNull List<@NotNull Long> degreeIds) {
}
