package ru.example.cloudfiles.service.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.DirectoryCreationException;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.DirectoryNotExistException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceSaveException;
import ru.example.cloudfiles.service.storage.MinioService;
import ru.example.cloudfiles.validation.PathValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadManager {

          private final MinioService minioService;

          private final FileOperationsManager fileOperations;

          private final PathValidator pathValidator;

          public List<String> prepareUpload(String uploadPath, MultipartFile[] files,
                                            String technicalPath) throws Exception {
                    validateUpload(uploadPath, files, technicalPath);

                    Set<String> directories =
                              fileOperations.collectParentDirectories(files, technicalPath);
                    createMissingDirectories(directories);

                    return getFileObjectNames(files, technicalPath);
          }

          public void executeUpload(MultipartFile file, String objectName) {
                    try {
                              minioService.uploadFile(file, objectName);
                              log.debug("Successfully uploaded file: {}", objectName);
                    } catch (Exception e) {
                              throw new ResourceSaveException(objectName, e);
                    }
          }

          private void validateUpload(String uploadPath, MultipartFile[] files,
                                      String technicalPath) throws Exception {
                    if (!uploadPath.isBlank()) {
                              pathValidator.validatePath(uploadPath);
                              if (!minioService.objectExists(technicalPath)) {
                                        throw new DirectoryNotExistException(uploadPath);
                              }
                    }

                    Arrays.stream(files)
                              .map(MultipartFile::getOriginalFilename)
                              .filter(Objects::nonNull)
                              .forEach(filename -> {
                                        pathValidator.validateFileName(filename);
                                        String fullPath = technicalPath + filename;
                                        try {
                                                  if (minioService.objectExists(fullPath)) {
                                                            throw new ResourceAlreadyExistsException(
                                                                      filename);
                                                  }
                                        } catch (Exception e) {
                                                  throw new RuntimeException(e);
                                        }
                              });
          }

          private void createMissingDirectories(Set<String> directories) {
                    directories.parallelStream()
                              .filter(directory -> {
                                        try {
                                                  return !minioService.objectExists(directory);
                                        } catch (Exception e) {
                                                  throw new RuntimeException(e);
                                        }
                              })
                              .forEach(directory -> {
                                        try {
                                                  fileOperations.createDirectory(directory);
                                                  log.debug("Created directory: {}", directory);
                                        } catch (Exception e) {
                                                  throw new DirectoryCreationException(directory,
                                                            e);
                                        }
                              });
          }

          private List<String> getFileObjectNames(MultipartFile[] files, String technicalPath) {
                    return Arrays.stream(files)
                              .filter(file -> file.getOriginalFilename() != null &&
                                        !file.getOriginalFilename().endsWith("/"))
                              .map(file -> technicalPath + file.getOriginalFilename())
                              .toList();
          }

          // Новый метод для параллельной загрузки файлов
          public void executeParallelUpload(List<MultipartFile> files, List<String> objectNames) {
                    if (files.size() != objectNames.size()) {
                              throw new IllegalArgumentException(
                                        "Files and object names lists must have the same size");
                    }

                    IntStream.range(0, files.size())
                              .parallel()
                              .forEach(i -> executeUpload(files.get(i), objectNames.get(i)));
          }
}