package com.blbulyandavbulyan.larm.api.assets;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/assets")
@Tag(name = "Assets", description = "Endpoints for managing and retrieving media assets")
interface AssetsApi {
    @GetMapping("/{mediaId}")
    @Operation(
            summary = "Get asset by media ID",
            description =
                    """
                    Retrieves the binary content (e.g., audio file) associated with a specific media ID. \
                    The content-type of the response depends on the stored asset.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Asset found and returned",
                            content = @Content(mediaType = "application/octet-stream")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Asset not found for the given media ID",
                            content = @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(implementation = ProblemDetail.class)
                            )
                    )
            }
    )
    ResponseEntity<Resource> getAsset(
            @Parameter(description = "UUID of the media asset to retrieve", required = true, in = ParameterIn.PATH)
            @PathVariable UUID mediaId);
}
