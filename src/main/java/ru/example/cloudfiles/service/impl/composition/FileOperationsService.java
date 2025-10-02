package ru.example.cloudfiles.service.impl.composition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.config.properties.S3Properties;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.entity.Resource;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.ZipCreationException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.mapper.DtoMapper;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileOperationsService {
          private final S3Repository s3Repo;

          private final PathManager paths;

          private final S3Properties props;

          private final DtoMapper dtoMapper;

          public ResourceInfoResponseDTO getResource(long userId, String path) {
                    try {
                              var resource = s3Repo.getResourceByPath(props.getDefaultBucketName(),
                                        paths.toTechnicalPath(userId, path));
                              return dtoMapper.toDto(userId, resource);
                    } catch (ResourceNotFoundException e) {
                              throw new ResourceNotFoundException(path);
                    }
          }

          public void deleteResource(long userId, String path) {
                    if (!resourceExists(userId, path)) throw new ResourceNotFoundException(path);
                    s3Repo.deleteResource(props.getDefaultBucketName(),
                              paths.toTechnicalPath(userId, path));
          }

          public DownloadResult prepareDownload(long userId, String path) {
                    StreamingResponseBody streamingBody = download(userId, path);
                    String fileName = extractFileName(path);
                    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                              .replace("+", "%20");
                    String contentDisposition =
                              "attachment; filename*=UTF-8''%s".formatted(encodedFileName);
                    return new DownloadResult(streamingBody, contentDisposition);
          }

          private String extractFileName(String path) {
                    String baseName = Paths.get(path).getFileName().toString();
                    return path.endsWith("/") ? baseName.concat(".zip") : baseName;
          }

          public StreamingResponseBody download(long userId, String path) {
                    var resourceNames = findAllNames(userId, path);
                    return resourceNames.size() == 1
                              ? createSingleFileResponse(resourceNames.getFirst())
                              : createZipResponse(userId, resourceNames);
          }

          public ResourceInfoResponseDTO moveResource(long userId, String oldPath, String newPath) {
                    validateMove(userId, oldPath, newPath);
                    String oldTech = paths.toTechnicalPath(userId, oldPath);
                    String newTech = paths.toTechnicalPath(userId, newPath);

                    if (paths.isDirectory(oldPath)) moveDir(oldTech, newTech);
                    else moveFile(oldTech, newTech);

                    s3Repo.deleteResource(props.getDefaultBucketName(), oldTech);
                    return dtoMapper.toDto(userId,
                              s3Repo.getResourceByPath(props.getDefaultBucketName(), newTech));
          }

          private StreamingResponseBody createSingleFileResponse(String resourceName) {
                    return os -> {
                              try (var is = s3Repo.getResourceByPath(props.getDefaultBucketName(),
                                        resourceName).dataStream()) {
                                        is.transferTo(os);
                              } catch (IOException e) {
                                        throw new ResourceNotFoundException(
                                                  "Failed to read file: " + resourceName);
                              }
                    };
          }

          private StreamingResponseBody createZipResponse(long userId, List<String> resourceNames) {
                    return os -> {
                              try (var zos = new ZipOutputStream(os)) {
                                        resourceNames.forEach(name -> addToZip(userId,
                                                  s3Repo.getResourceByPath(
                                                            props.getDefaultBucketName(), name),
                                                  zos));
                              }
                    };
          }

          private void addToZip(long userId, Resource resource, ZipOutputStream zos) {
                    try {
                              zos.putNextEntry(
                                        new ZipEntry(paths.toUserPath(userId, resource.path())));
                              if (!paths.isDirectory(resource.path())) {
                                        try (var is = resource.dataStream()) {
                                                  is.transferTo(zos);
                                        }
                              }
                              zos.closeEntry();
                    } catch (IOException e) {
                              throw new ZipCreationException(
                                        "Failed to add file to zip: " + resource.path(), e);
                    }
          }

          private void moveDir(String oldTech, String newTech) {
                    String oldPrefix = oldTech.endsWith("/") ? oldTech : oldTech + "/";
                    String newPrefix = newTech.endsWith("/") ? newTech : newTech + "/";

                    List<String> sourcePaths =
                              s3Repo.findAllNamesByPrefix(props.getDefaultBucketName(), oldPrefix,
                                        false);

                    sourcePaths.forEach(oldName -> {
                              String relativePath = oldName.substring(oldPrefix.length());
                              String newName = newPrefix + relativePath;

                              var resource = s3Repo.getResourceByPath(props.getDefaultBucketName(),
                                        oldName);
                              byte[] data;
                              try (InputStream is = resource.dataStream()) {
                                        data = is.readAllBytes();
                              } catch (IOException e) {
                                        throw new ResourceNotFoundException(
                                                  "Failed to read resource: " + oldName);
                              }
                              s3Repo.saveResource(props.getDefaultBucketName(), newName,
                                        new ByteArrayInputStream(data));
                    });

                    sourcePaths.forEach(
                              oldName -> s3Repo.deleteResource(props.getDefaultBucketName(),
                                        oldName));
          }

          private void moveFile(String oldTech, String newTech) {
                    var resource = s3Repo.getResourceByPath(props.getDefaultBucketName(), oldTech);
                    s3Repo.saveResource(props.getDefaultBucketName(), newTech,
                              resource.dataStream());
          }

          private void validateMove(long userId, String oldPath, String newPath) {
                    if (!resourceExists(userId, oldPath))
                              throw new ResourceNotFoundException(oldPath);
                    if (resourceExists(userId, newPath))
                              throw new ResourceAlreadyExistsException(newPath);
          }

          private boolean resourceExists(long userId, String path) {
                    return s3Repo.isObjectExists(props.getDefaultBucketName(),
                              paths.toTechnicalPath(userId, path));
          }

          private List<String> findAllNames(long userId, String prefix) {
                    return s3Repo.findAllNamesByPrefix(props.getDefaultBucketName(),
                              paths.toTechnicalPath(userId, prefix), true);
          }
}