package ru.example.cloudfiles.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.example.cloudfiles.dto.DownloadResult;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;
import ru.example.cloudfiles.service.S3UserService;
import ru.example.cloudfiles.service.impl.composition.DirectoryOperationsService;
import ru.example.cloudfiles.service.impl.composition.FileOperationsService;
import ru.example.cloudfiles.service.impl.composition.SearchService;
import ru.example.cloudfiles.service.impl.composition.UploadService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UserServiceImpl implements S3UserService {

          private final FileOperationsService fileOps;

          private final DirectoryOperationsService dirOps;

          private final UploadService uploadService;

          private final SearchService searchService;


          @Override
          public ResourceInfoResponseDTO getResource(long userId, String path) {
                    return fileOps.getResource(userId, path);
          }

          @Override
          public void deleteResource(long userId, String path) {
                    fileOps.deleteResource(userId, path);
          }

          @Override
          public void createUserDir(long userId) {
                    dirOps.createUserDir(userId);
          }

          @Override
          public DownloadResult prepareDownload(long userId, String path) {
                    return fileOps.prepareDownload(userId, path);
          }

          @Override
          public ResourceInfoResponseDTO moveResource(long userId, String oldPath, String newPath) {
                    return fileOps.moveResource(userId, oldPath, newPath);
          }

          @Override
          public List<ResourceInfoResponseDTO> search(long userId, String query) {
                    return searchService.search(userId, query);
          }

          @Override
          public List<ResourceInfoResponseDTO> upload(long userId, String uploadPath,
                                                      MultipartFile[] files) {
                    return uploadService.upload(userId, uploadPath, files);
          }

          @Override
          public List<ResourceInfoResponseDTO> getDir(long userId, String path) {
                    return dirOps.getDir(userId, path);
          }

          @Override
          public ResourceInfoResponseDTO createDir(long userId, String path) {
                    return dirOps.createDir(userId, path);
          }
}