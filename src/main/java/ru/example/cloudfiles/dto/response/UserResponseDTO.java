package ru.example.cloudfiles.dto.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserResponseDTO(
        @NotNull
        @NotEmpty
        @Size(min = 5, max = 20)
        String username) {
}