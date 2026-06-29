package com.blbulyandavbulyan.larm.api.phrases;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/phrases")
@Tag(name = "Phrases", description = "Endpoints for managing the collection of Armenian phrases, their translations, and audio assets")
interface PhrasesApi {
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get paged phrases",
            description =
                    """
                    Retrieves a paginated list of phrases stored in the system, including their transcriptions, \
                    translations in various languages, and links to audio assets.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the requested page of phrases",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PhrasesPagedResponse.class)
                            )
                    )
            }
    )
    PhrasesPagedResponse getPhrases(@ParameterObject @Validated PageRequest pageRequest);

}
