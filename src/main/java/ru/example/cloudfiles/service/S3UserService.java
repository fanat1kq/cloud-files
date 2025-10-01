package ru.example.cloudfiles.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.exception.StorageOperationImpl.directory.NotDirectoryException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceAlreadyExistsException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceRetrievalException;
import ru.example.cloudfiles.exception.StorageOperationImpl.resource.ResourceSaveException;
import ru.example.cloudfiles.service.manager.FileOperationsManager;
import ru.example.cloudfiles.service.manager.SearchManager;
import ru.example.cloudfiles.service.manager.UploadManager;
import ru.example.cloudfiles.service.storage.MinioService;
import ru.example.cloudfiles.service.storage.PathResolver;
import ru.example.cloudfiles.service.storage.ResourceInfoService;
import ru.example.cloudfiles.validation.FileUploadValidator;
import ru.example.cloudfiles.validation.PathValidator;

import java.io.OutputStream;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
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
                    return resourceInfoService.getResourceInfo(userId, resolvePath(userId, path));
          }

          public void deleteResource(long userId, String path) throws Exception {
                    fileOperations.delete(resolvePath(userId, path));
          }

          @SneakyThrows
          public StreamingResponseBody downloadResource(long userId, String path) {
                    String objectName = resolvePath(userId, path);
                    return path.endsWith("/")
                              ? zipService.createZipFromDirectory(objectName)
                              : out -> downloadFile(objectName, out);
          }

          public ResourceInfoResponseDTO moveResource(long userId, String from, String to)
                    throws Exception {
                    pathValidator.validateMovePaths(from, to);
                    String fromPath = resolvePath(userId, from);
                    String toPath = pathResolver.resolveUserPath(userId, to);
                    if (minioService.objectExists(toPath)) throw new ResourceAlreadyExistsException(to);
                    fileOperations.move(fromPath, toPath);
                    return resourceInfoService.getResourceInfo(userId, toPath);
          }

          @SneakyThrows
          public List<ResourceInfoResponseDTO> searchResource(long userId, String query) {
                    return searchManager.searchObjects(userId, query).stream()
                              .map(name -> {
                                        try {
                                                  return resourceInfoService.getResourceInfo(userId, name);
                                        } catch (Exception e) {
                                                  throw new RuntimeException(e);
                                        }
                              })
                              .toList();
          }

          @SneakyThrows
          public List<ResourceInfoResponseDTO> uploadFiles(long userId, String uploadPath, MultipartFile[] files) {
                    pathValidator.validatePath(uploadPath);
                    fileUploadValidator.validateFiles(files);
                    String techPath = pathResolver.resolveAndNormalizeDirectory(userId, uploadPath);
                    List<String> objectNames = uploadManager.prepareUpload(uploadPath, files, techPath);
                    uploadFilesParallel(files, objectNames);
                    return resourceInfoService.getResourcesInfo(userId, objectNames);
          }

          public List<ResourceInfoResponseDTO> getDirectoryContents(long userId, String path) {
                    String techPath = resolveDirectory(userId, path);
                    return resourceInfoService.getResourcesInfo(userId, getFilteredObjects(userId, techPath));
          }

          public ResourceInfoResponseDTO createDirectory(long userId, String path)
                    throws Exception {
                    if (!path.endsWith("/")) throw new NotDirectoryException(path);
                    String techPath = pathResolver.resolveUserPath(userId, path);
                    if (minioService.objectExists(techPath)) throw new ResourceAlreadyExistsException(path);
                    fileOperations.createDirectory(techPath);
                    return resourceInfoService.getResourceInfo(userId, techPath);
          }

          @SneakyThrows
          public void createUserDirectory(long userId)  {
                    minioService.createDirectory("user-%d-files/".formatted(userId));
          }

          @SneakyThrows
          private String resolvePath(long userId, String path)  {
                    pathValidator.validatePath(path);
                    String objectName = pathResolver.resolveAndNormalize(userId, path);
                    pathResolver.ensurePathExists(objectName, path);
                    return objectName;
          }

          @SneakyThrows
          private String resolveDirectory(long userId, String path) {
                    String techPath = pathResolver.resolveAndNormalizeDirectory(userId, path);

                    if (path.isBlank()) pathResolver.ensureUserDirectoryExists(userId);
                    else pathResolver.ensurePathExists(techPath, path);

                    return techPath;
          }

          private void downloadFile(String objectName, OutputStream out) {
                    try (var stream = minioService.getObjectStream(objectName)) {
                              stream.transferTo(out);
                    } catch (Exception e) {
                              throw new ResourceRetrievalException("Download failed: " + objectName, e);
                    }
          }

          private void uploadFilesParallel(MultipartFile[] files, List<String> objectNames) {
                    IntStream.range(0, files.length).parallel()
                              .filter(i -> files[i].getOriginalFilename() != null && !files[i].getOriginalFilename().endsWith("/"))
                              .forEach(i -> {
                                        try { uploadManager.executeUpload(files[i], objectNames.get(i)); }
                                        catch (Exception e) { throw new ResourceSaveException(objectNames.get(i), e); }
                              });
          }

          @SneakyThrows
          private List<String> getFilteredObjects(long userId, String techPath) {
                    return minioService.listObjects(techPath, false).stream()
                              .filter(name -> !pathResolver.isRootDirectory(name, userId) && !name.equals(techPath))
                              .toList();
          }
}