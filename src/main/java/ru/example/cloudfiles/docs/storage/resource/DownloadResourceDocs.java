package ru.example.cloudfiles.docs.storage.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import ru.example.cloudfiles.dto.ErrorResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Operation(summary = "Download Resource", description = "Downloading resource from bucket",
        tags = {"Storage", "Resource"})
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid path format",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "User not authenticated",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "User not authenticated"))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class, description = "Resource not found")
                )),
        @ApiResponse(responseCode = "500", description = "Can't download empty directory",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "Unknown Error")))
})
public @interface DownloadResourceDocs {
}
