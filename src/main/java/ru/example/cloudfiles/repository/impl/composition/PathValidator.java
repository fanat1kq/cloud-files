package ru.example.cloudfiles.repository.impl.composition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class PathValidator {
          private static final Pattern FORBIDDEN_SYMBOLS = Pattern.compile(".*[\\\\/?*:<>\"|].*");

          public void validatePath(String path) {
//                    if (path == null || path.isBlank()) {
//                              throw new EmptyPathException();
//                    }
//
//                    if (FORBIDDEN_SYMBOLS.matcher(path).matches()) {
//                              throw new ForbiddenSymbolException();
//                    }
//
//                    if (path.contains("//")) {
//                              throw new EmptyPathException();
//                    }
          }
}
