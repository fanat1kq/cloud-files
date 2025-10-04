package ru.example.cloudfiles.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(

        @NotNull
        @NotEmpty
        @Size(min = 5, max = 20)
        String username,

        @NotNull
        @NotEmpty
        @Size(min = 8)
        String password) {
}