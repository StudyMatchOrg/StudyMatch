package pt.upt.studymatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank @Size(max = 20) String studentNumber,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email) {
}
