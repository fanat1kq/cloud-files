package ru.example.cloudfiles.service;


import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;

import java.util.List;

public interface S3Service {

    ResourceInfoResponseDTO getResource(long userId, String path);

    void deleteResource(long userId, String path);

    void createUserDirection(long userId);

    DownloadResult prepareDownload(long userId, String path);

    ResourceInfoResponseDTO moveResource(long userId, String oldPath, String newPath);

    List<ResourceInfoResponseDTO> searchResource(long userId, String query);

    List<ResourceInfoResponseDTO> uploadResource(long userId, String uploadPath, MultipartFile[] files);

    List<ResourceInfoResponseDTO> getDirection(long userId, String path);

    ResourceInfoResponseDTO createDirection(long userId, String path);
}