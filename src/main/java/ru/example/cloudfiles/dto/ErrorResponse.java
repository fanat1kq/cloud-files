package ru.example.cloudfiles.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse {

        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public ErrorResponse(String message) {

        this(message, LocalDateTime.now());
    }

    public ErrorResponse() {

        this(null, LocalDateTime.now());
    }
}