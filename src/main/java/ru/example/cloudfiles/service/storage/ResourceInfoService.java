package ru.example.cloudfiles.service.storage;

import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.mapper.ResourceMapper;
import ru.example.cloudfiles.mapper.model.PathInfo;
import ru.example.cloudfiles.mapper.model.ResourceMappingData;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceInfoService {

          private static final String USER_DIRECTORY = "user-%d-files/";

          private final MinioService minioService;

          private final ResourceMapper resourceMapper;

          public ResourceInfoResponseDTO getResourceInfo(long userId, String objectName)
                    throws Exception {
                    StatObjectResponse stat = minioService.getObjectStats(objectName);

                    String userPrefix = USER_DIRECTORY.formatted(userId);
                    boolean isDirectory = objectName.endsWith("/") || stat.size() == 0;
                    String relativePath = objectName.substring(userPrefix.length());

                    ResourceMappingData mappingData = new ResourceMappingData(
                              objectName,
                              relativePath,
                              stat.size(),
                              isDirectory,
                              userId
                    );

                    PathInfo pathInfo =
                              resourceMapper.extractPathInfo(relativePath, isDirectory);

                    return resourceMapper.toResourceInfoDTO(mappingData, pathInfo);
          }

          public List<ResourceInfoResponseDTO> getResourcesInfo(long userId,
                                                                List<String> objectNames) {
                    return objectNames.stream()
                              .map(objectName -> {
                                        try {
                                                  return getResourceInfo(userId, objectName);
                                        } catch (Exception e) {
                                                  throw new RuntimeException(
                                                            "Failed to get resource info for: " +
                                                                      objectName, e);
                                        }
                              })
                              .toList();
          }
}