package ru.example.cloudfiles.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.cloudfiles.exception.validation.EmptyPathException;
import ru.example.cloudfiles.exception.validation.ForbiddenSymbolException;

import java.util.regex.Pattern;

@Component
@Slf4j
public class PathValidator {

    private static final Pattern FORBIDDEN_SYMBOLS = Pattern.compile("[\\\\/:*?\"<>|]");

    public void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new EmptyPathException();
        }

        if (FORBIDDEN_SYMBOLS.matcher(path).matches()) {
            throw new ForbiddenSymbolException();
        }

        if (path.contains("//")) {
            throw new EmptyPathException();
        }
    }
}
