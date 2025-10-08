package ru.example.cloudfiles.service.impl.composition.fileOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.config.properties.MinioProperties;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.exception.storageOperationImpl.directory.ZipCreationException;
import ru.example.cloudfiles.exception.storageOperationImpl.resource.ResourceNotFoundException;
import ru.example.cloudfiles.repository.S3Repository;
import ru.example.cloudfiles.service.impl.PathManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileDownloadService {

    private final S3Repository s3Repo;
    private final FileQueryService fileQueryService;
    private final PathManager paths;
    private final MinioProperties props;

    public DownloadResult prepareDownload(long userId, String path) {

        log.info("Download prepared - userId: {}, path: '{}'", userId, path);

        return new DownloadResult(download(userId, path), ContentDisposition.attachment()
                .filename(extractFileName(path), StandardCharsets.UTF_8)
                .build()
                .toString()
        );
    }

    public StreamingResponseBody download(long userId, String path) {

        log.info("Download started - userId: {}, path: '{}'", userId, path);

        var resourceNames = fileQueryService.findAllNames(userId, path);

        if (resourceNames.size() == 1) {
            log.debug("Single file download - userId: {}, file: '{}'", userId, resourceNames.getFirst());
            return createSingleFileResponse(resourceNames.getFirst());
        } else {
            log.debug("ZIP download - userId: {}, files: {}", userId, resourceNames.size());
            return createZipResponse(userId, resourceNames);
        }
    }

    private String extractFileName(String path) {

        String baseName = Paths.get(path).getFileName().toString();
        return path.endsWith("/") ? baseName + ".zip" : baseName;
    }

    private StreamingResponseBody createSingleFileResponse(String resourceName) {

        return os -> {
            try (var is = s3Repo.getResourceByPath(props.getBucket(), resourceName).dataStream()) {
                is.transferTo(os);
                log.debug("Single file downloaded successfully - file: '{}'", resourceName);
            } catch (IOException e) {
                log.error("Single file download failed - file: '{}'", resourceName, e);
                throw new ResourceNotFoundException(resourceName);
            }
        };
    }

    private StreamingResponseBody createZipResponse(long userId, List<String> resourceNames) {

        return outputStream -> {
            try (var zipOutputStream = new ZipOutputStream(outputStream)) {
                resourceNames.forEach(name -> addToZip(userId, name, zipOutputStream));
                log.debug("ZIP archive created successfully - files: {}", resourceNames.size());
            } catch (Exception e) {
                log.error("ZIP creation failed - userId: {}, files: {}", userId, resourceNames.size(), e);
                throw e;
            }
        };
    }

    private void addToZip(long userId, String resourceName, ZipOutputStream zipOutputStream) {

        try {
            var resource = s3Repo.getResourceByPath(props.getBucket(), resourceName);
            zipOutputStream.putNextEntry(new ZipEntry(paths.toUserPath(userId, resource.path())));
            if (!paths.isDirectory(resource.path())) {
                try (var dataStream = resource.dataStream()) {
                    dataStream.transferTo(zipOutputStream);
                }
            }
            zipOutputStream.closeEntry();
            log.trace("File added to ZIP - '{}'", resourceName);
        } catch (IOException e) {
            log.error("Failed to add file to ZIP - '{}'", resourceName, e);
            throw new ZipCreationException(resourceName, e);
        }
    }
}