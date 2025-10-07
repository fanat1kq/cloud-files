package ru.example.cloudfiles.dto;

import java.io.InputStream;
import java.util.Objects;

public record Resource(String path, InputStream dataStream, long size) {

    public Resource {
        Objects.requireNonNull(dataStream);
    }
}