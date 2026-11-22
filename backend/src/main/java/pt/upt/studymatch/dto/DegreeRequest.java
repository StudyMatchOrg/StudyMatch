package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DegreeRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 150) String name) {
}
