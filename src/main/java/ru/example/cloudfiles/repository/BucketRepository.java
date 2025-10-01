package ru.example.cloudfiles.repository;

public interface BucketRepository {

          void deleteBucket(String bucketName);

          boolean isBucketExists(String bucketName);
}