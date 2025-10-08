package ru.example.cloudfiles.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.InputStream;

public record Resource(
        @NotBlank
        @Pattern(regexp = "^/.*$", message = "Path must start with '/'")
        String path,

        @NotNull
        InputStream dataStream,

        @Min(0)
        long size
) {
}