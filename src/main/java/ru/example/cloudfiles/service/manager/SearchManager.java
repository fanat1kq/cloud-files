package ru.example.cloudfiles.service.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.example.cloudfiles.service.storage.MinioService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchManager {

          private final MinioService minioService;

          public List<String> searchObjects(long userId, String query) throws Exception {
                    if (query == null || query.isBlank()) {
                              throw new IllegalArgumentException("Search query cannot be empty");
                    }

                    String userDirectory = "user-%d-files/".formatted(userId);
                    return minioService.listAllObjects(userDirectory).stream()
                              .filter(objectName -> objectName.substring(userDirectory.length())
                                        .toLowerCase().contains(query.toLowerCase()))
                              .toList();
          }
}