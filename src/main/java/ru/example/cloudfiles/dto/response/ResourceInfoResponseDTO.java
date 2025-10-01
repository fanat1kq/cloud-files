package ru.example.cloudfiles.dto.response;

import lombok.Builder;
import ru.example.cloudfiles.dto.ResourceType;

@Builder
public record ResourceInfoResponseDTO(
          String path,
          String name,
          long size,
          ResourceType type
) {
}

