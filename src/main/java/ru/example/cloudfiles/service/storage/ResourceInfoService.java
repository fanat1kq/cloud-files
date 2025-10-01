package ru.example.cloudfiles.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.mapper.model.ResourceMappingData;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class ResourceInfoService {

          private final MinioService minioService;

          private final ResourceMapper resourceMapper;

          @SneakyThrows
          public ResourceInfoResponseDTO getResourceInfo(long userId, String objectName) {

                    var stat = minioService.getObjectStats(objectName);
                    String userPrefix = "user-%d-files/".formatted(userId);

                    return resourceMapper.toResourceInfoDTO(
                              new ResourceMappingData(objectName,
                                        objectName.substring(userPrefix.length()),
                                        stat.size(), objectName.endsWith("/") || stat.size() == 0,
                                        userId),
                              resourceMapper.extractPathInfo(
                                        objectName.substring(userPrefix.length()),
                                        objectName.endsWith("/") || stat.size() == 0)
                    );
          }

          public List<ResourceInfoResponseDTO> getResourcesInfo(long userId,
                                                                List<String> objectNames) {

                    return objectNames.stream()
                              .map(name -> {
                                        try {
                                                  return getResourceInfo(userId, name);
                                        } catch (Exception e) {
                                                  log.warn("Failed to get info for: {}", name, e);
                                                  return null;
                                        }
                              })
                              .filter(Objects::nonNull)
                              .toList();
          }
}