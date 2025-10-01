package ru.example.cloudfiles.repository;

import ru.example.cloudfiles.entity.Resource;

import java.io.InputStream;
import java.util.List;

public interface ObjectRepository {

          void deleteResource(String bucketName, String path);

          List<String> findAllNamesByPrefix(String bucket, String prefix, boolean recursive);

          Resource getResourceByPath(String bucket, String path);

          void saveResource(String bucket, String path, InputStream dataStream);

          boolean isObjectExists(String bucketName, String path);
}
