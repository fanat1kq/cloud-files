package ru.example.cloudfiles.repository;

import ru.example.cloudfiles.entity.Resource;

import java.io.InputStream;
import java.util.List;

public interface S3Repository {

          void deleteBucket(String bucketName);

          boolean isBucketExists(String bucketName);

          void deleteResource(String bucketName, String path);

          List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive);

          Resource getResourceByPath(String bucket, String path);

          void saveResource(String bucket, String path, InputStream dataStream);

          void createDirectory(String bucketName, String path);

          boolean isObjectExists(String bucketName, String path);
}