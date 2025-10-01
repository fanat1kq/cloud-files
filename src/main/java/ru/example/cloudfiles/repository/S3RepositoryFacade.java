package ru.example.cloudfiles.repository;

import org.springframework.stereotype.Repository;
import ru.example.cloudfiles.entity.Resource;

import java.io.InputStream;
import java.util.List;

@Repository
public class S3RepositoryFacade implements S3Repository {

          private final BucketRepository bucketRepository;
          private final ObjectRepository objectRepository;
          private final DirectoryRepository directoryRepository;

          public S3RepositoryFacade(BucketRepository bucketRepository,
                                    ObjectRepository objectRepository,
                                    DirectoryRepository directoryRepository) {
                    this.bucketRepository = bucketRepository;
                    this.objectRepository = objectRepository;
                    this.directoryRepository = directoryRepository;
          }

          @Override
          public void deleteBucket(String bucketName) {
                    bucketRepository.deleteBucket(bucketName);
          }

          @Override
          public boolean isBucketExists(String bucketName) {
                    return bucketRepository.isBucketExists(bucketName);
          }

          @Override
          public List<String> findAllNamesByPrefix(String bucket, String prefix,
                                                   boolean recursive) {
                    return objectRepository.findAllNamesByPrefix(bucket, prefix, recursive);
          }

          @Override
          public void deleteResource(String bucketName, String path) {
                    objectRepository.deleteResource(bucketName, path);
          }

          @Override
          public Resource getResourceByPath(String bucket, String path) {
                    return objectRepository.getResourceByPath(bucket, path);
          }

          @Override
          public void saveResource(String bucket, String path, InputStream dataStream) {
                    objectRepository.saveResource(bucket, path, dataStream);
          }

          @Override
          public void createDirectory(String bucketName, String path) {
                    directoryRepository.createDirectory(bucketName, path);
          }

          @Override
          public boolean isObjectExists(String bucketName, String path) {
                    return objectRepository.isObjectExists(bucketName, path);
          }
}