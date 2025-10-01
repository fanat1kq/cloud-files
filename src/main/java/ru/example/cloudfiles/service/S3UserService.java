package ru.example.cloudfiles.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.NotDirectoryException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceRetrievalException;
import ru.example.cloudfiles.service.manager.FileOperationsManager;
import ru.example.cloudfiles.service.manager.SearchManager;
import ru.example.cloudfiles.service.manager.UploadManager;
import ru.example.cloudfiles.service.storage.MinioService;
import ru.example.cloudfiles.service.storage.PathResolver;
import ru.example.cloudfiles.service.storage.ResourceInfoService;
import ru.example.cloudfiles.validation.FileUploadValidator;
import ru.example.cloudfiles.validation.PathValidator;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class S3UserService {

          private final ResourceInfoService resourceInfoService;

          private final ZipService zipService;

          private final FileOperationsManager fileOperations;

          private final PathResolver pathResolver;

          private final UploadManager uploadManager;

          private final SearchManager searchManager;

          private final PathValidator pathValidator;

          private final FileUploadValidator fileUploadValidator;

          private final MinioService minioService;

          public ResourceInfoResponseDTO getResource(long userId, String path) throws Exception {
                    pathValidator.validatePath(path);
                    String objectName = pathResolver.resolveUserPath(userId, path);
                    pathResolver.ensurePathExists(objectName, path);

                    return resourceInfoService.getResourceInfo(userId, objectName);
          }

          public void deleteResource(long userId, String path) throws Exception {
                    pathValidator.validatePath(path);
                    String objectName = pathResolver.resolveAndNormalize(userId, path);
                    pathResolver.ensurePathExists(objectName, path);

                    fileOperations.delete(objectName);
          }

          public StreamingResponseBody downloadResource(long userId, String path) throws Exception {
                    pathValidator.validatePath(path);
                    String objectName = pathResolver.resolveAndNormalize(userId, path);
                    pathResolver.ensurePathExists(objectName, path);

                    if (path.endsWith("/")) {
                              return zipService.createZipFromDirectory(objectName);
                    } else {
                              return outputStream -> {
                                        try (var stream = minioService.getObjectStream(
                                                  objectName)) {
                                                  stream.transferTo(
                                                            outputStream); // Java 9+ более эффективный метод
                                        } catch (Exception e) {
                                                  throw new ResourceRetrievalException(
                                                            "File download failed: " + objectName,
                                                            e);
                                        }
                              };
                    }
          }

          public ResourceInfoResponseDTO moveResource(long userId, String from, String to)
                    throws Exception {
                    pathValidator.validateMovePaths(from, to);

                    String fromPath = pathResolver.resolveUserPath(userId, from);
                    String toPath = pathResolver.resolveUserPath(userId, to);

                    pathResolver.ensurePathExists(fromPath, from);
                    if (minioService.objectExists(toPath)) {
                              throw new ResourceAlreadyExistsException(to);
                    }

                    fileOperations.move(fromPath, toPath);
                    return resourceInfoService.getResourceInfo(userId, toPath);
          }

          public List<ResourceInfoResponseDTO> searchResource(long userId, String query)
                    throws Exception {
                    List<String> foundObjects = searchManager.searchObjects(userId, query);
                    return resourceInfoService.getResourcesInfo(userId, foundObjects);
          }

          public List<ResourceInfoResponseDTO> uploadFiles(long userId, String uploadPath,
                                                           MultipartFile[] files) throws Exception {
                    pathValidator.validatePath(uploadPath);
                    fileUploadValidator.validateFiles(files);

                    String technicalPath =
                              pathResolver.resolveAndNormalizeDirectory(userId, uploadPath);
                    List<String> objectNames =
                              uploadManager.prepareUpload(uploadPath, files, technicalPath);

                    Arrays.stream(files)
                              .filter(file -> file.getOriginalFilename() != null &&
                                        !file.getOriginalFilename().endsWith("/"))
                              .parallel() // Параллельная обработка
                              .forEach(file -> {
                                        int index = Arrays.asList(files).indexOf(file);
                                        try {
                                                  uploadManager.executeUpload(file,
                                                            objectNames.get(index));
                                        } catch (Exception e) {
                                                  throw new RuntimeException(e);
                                        }
                              });

                    return resourceInfoService.getResourcesInfo(userId, objectNames);
          }

          public List<ResourceInfoResponseDTO> getDirectoryContents(long userId, String path)
                    throws Exception {
                    String technicalPath = pathResolver.resolveAndNormalizeDirectory(userId, path);

                    if (path.isBlank()) {
                              pathResolver.ensureUserDirectoryExists(userId);
                    } else {
                              pathResolver.ensurePathExists(technicalPath, path);
                    }

                    List<String> objects = minioService.listObjects(technicalPath, false).stream()
                              .filter(objectName ->
                                        !pathResolver.isRootDirectory(objectName, userId) &&
                                                  !objectName.equals(technicalPath))
                              .toList(); // Java 16+ toList() вместо collect(Collectors.toList())

                    return resourceInfoService.getResourcesInfo(userId, objects);
          }

          public ResourceInfoResponseDTO createDirectory(long userId, String path)
                    throws Exception {
                    if (!path.endsWith("/")) {
                              throw new NotDirectoryException(path);
                    }

                    String technicalPath = pathResolver.resolveUserPath(userId, path);
                    if (minioService.objectExists(technicalPath)) {
                              throw new ResourceAlreadyExistsException(path);
                    }

                    fileOperations.createDirectory(technicalPath);
                    return resourceInfoService.getResourceInfo(userId, technicalPath);
          }

          public void createUserDirectory(long userId) throws Exception {
                    String directoryName = "user-%d-files/".formatted(userId);
                    minioService.createDirectory(directoryName);
          }
}