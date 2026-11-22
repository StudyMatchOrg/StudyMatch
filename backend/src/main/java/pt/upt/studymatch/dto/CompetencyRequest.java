package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A new competency. The code (e.g. {@code PROG}) cannot be changed later. */
public record CompetencyRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300) String description) {
}
