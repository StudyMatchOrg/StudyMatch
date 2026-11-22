package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A new name and description for an existing competency. */
public record CompetencyChange(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300) String description) {
}
