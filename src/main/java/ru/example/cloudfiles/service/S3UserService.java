package ru.example.cloudfiles.service;


import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;

import java.util.List;

public interface S3UserService {

          ResourceInfoResponseDTO getResource(long userId, String path);

          void deleteResource(long userId, String path);

          void createUserDir(long userId);

          DownloadResult prepareDownload(long userId, String path);

          ResourceInfoResponseDTO moveResource(long userId, String oldPath, String newPath);

          List<ResourceInfoResponseDTO> search(long userId, String query);

          List<ResourceInfoResponseDTO> upload(long userId, String uploadPath,
                                               MultipartFile[] files);

          List<ResourceInfoResponseDTO> getDir(long userId, String path);

          ResourceInfoResponseDTO createDir(long userId, String path);
}