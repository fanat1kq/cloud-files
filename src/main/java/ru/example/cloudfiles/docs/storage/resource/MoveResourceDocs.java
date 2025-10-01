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
@Operation(
        summary = "Upload Resource",
        description = "Uploading one or multiple resources to the specified folder in the bucket. Supports nested directories.",
        tags = {"Storage", "Resource"}
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request body",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "Invalid multipart data or missing required parameters"))),

        @ApiResponse(responseCode = "401", description = "User not authenticated",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "User not authenticated"))),

        @ApiResponse(responseCode = "409", description = "File already exists",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "File with the same name already exists in the target path"))),

        @ApiResponse(responseCode = "500", description = "Unknown error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "Unexpected server error occurred during upload")))
})
public @interface MoveResourceDocs {
}
