package ru.example.cloudfiles.service;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.service.storage.MinioService;
import ru.example.cloudfiles.util.FilePathUtils;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

          private final MinioService minioService;
          private final FilePathUtils filePathUtils;

          public ZipService(MinioService minioService, FilePathUtils filePathUtils) {
                    this.minioService = minioService;
                    this.filePathUtils = filePathUtils;
          }

          public StreamingResponseBody createZipFromDirectory(String directoryPath) {
                    return outputStream -> {
                              try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                                        List<String> allObjects = minioService.listAllObjects(directoryPath);
                                        int prefixLength = directoryPath.length() -
                                                  Paths.get(directoryPath).getFileName().toString().length() - 1;

                                        for (String objectName : allObjects) {
                                                  if (!filePathUtils.isDirectory(objectName)) {
                                                            addFileToZip(zipOutputStream, objectName, prefixLength);
                                                  }
                                        }
                              } catch (Exception exception) {
                                        throw new RuntimeException("Directory download failed", exception);
                              }
                    };
          }

          private void addFileToZip(ZipOutputStream zipOutputStream, String objectName,
                                    int prefixLength) throws Exception {
                    try (InputStream stream = minioService.getObjectStream(objectName)) {
                              String entryName = objectName.substring(prefixLength);
                              ZipEntry zipEntry = new ZipEntry(entryName);
                              zipOutputStream.putNextEntry(zipEntry);
                              IOUtils.copy(stream, zipOutputStream, filePathUtils.getBufferSize());
                              zipOutputStream.closeEntry();
                    }
          }
}