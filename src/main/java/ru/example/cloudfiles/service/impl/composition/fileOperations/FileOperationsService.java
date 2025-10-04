package ru.example.cloudfiles.service.impl.composition.fileOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileOperationsService {
    private final FileQueryService fileQueryService;
    private final FileDownloadService fileDownloadService;
    private final FileMoveService fileMoveService;
    private final FileDeleteService fileDeleteService;

    public ResourceInfoResponseDTO getResource(long userId, String path) {
        return fileQueryService.getResource(userId, path);
    }

    public void deleteResource(long userId, String path) {
        fileDeleteService.deleteResource(userId, path);
    }

    public DownloadResult prepareDownload(long userId, String path) {
        return fileDownloadService.prepareDownload(userId, path);
    }

    public StreamingResponseBody download(long userId, String path) {
        return fileDownloadService.download(userId, path);
    }

    public ResourceInfoResponseDTO moveResource(long userId, String oldPath, String newPath) {
        return fileMoveService.moveResource(userId, oldPath, newPath);
    }
}