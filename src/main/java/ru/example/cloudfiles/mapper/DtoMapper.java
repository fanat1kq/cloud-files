package ru.example.cloudfiles.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.entity.Resource;
import ru.example.cloudfiles.service.impl.PathManager;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class DtoMapper {
          private final PathManager paths;

          public ResourceInfoResponseDTO toDto(long userId, Resource resource) {
                    boolean isDir = paths.isDirectory(resource.path());
                    String userPath = paths.toUserPath(userId, resource.path());
                    Path pathObj = Paths.get(userPath);

                    String path = extractPath(userId, resource, isDir, pathObj);
                    String fileName = isDir ? pathObj.getFileName() + "/" : pathObj.getFileName().toString();

                    return new ResourceInfoResponseDTO(path, fileName, resource.size(),
                              isDir ? ResourceType.DIRECTORY : ResourceType.FILE);
          }

          private String extractPath(long userId, Resource resource, boolean isDir, Path pathObj) {
                    String userDir = paths.getUserDirectory(userId);
                    int fileNameLen = pathObj.getFileName().toString().length();
                    int endIndex = resource.path().length() - fileNameLen - (isDir ? 1 : 0);
                    return resource.path().substring(userDir.length(), endIndex);
          }
}