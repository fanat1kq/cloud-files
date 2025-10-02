package ru.example.cloudfiles.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.s3")
@Getter
@Setter
public class S3Properties {

          private String defaultBucketName;
          private String userDirectoryPattern = "user-%d-files/";
          private int bufferSize = 1024;
}