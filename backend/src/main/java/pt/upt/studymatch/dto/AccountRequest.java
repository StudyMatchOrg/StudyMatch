package pt.upt.studymatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.upt.studymatch.domain.Role;

public record AccountRequest(
        @NotBlank @Size(max = 30) String username,
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Role role) {
}
