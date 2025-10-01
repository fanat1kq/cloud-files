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
        summary = "Search Resources",
        description = "Search resources in user's storage using a query string. Returns matching files and directories.",
        tags = {"Storage", "Resource"}
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid or missing search query",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "Query parameter is missing or invalid"))),

        @ApiResponse(responseCode = "401", description = "User not authenticated",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "User not authenticated"))),

        @ApiResponse(responseCode = "500", description = "Unknown error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class,
                                description = "Unexpected server error occurred during search")))
})
public @interface SearchResourceDocs {
}
