package ru.example.cloudfiles.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResult(StreamingResponseBody streamingBody, String contentDisposition) {}
