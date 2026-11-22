package com.blbulyandavbulyan.larm.api.learning;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.advice.ValidationErrorResponse;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/learning-items")
@Tag(name = "Learning items", description = "Enrol content (dialogues) for spaced repetition study")
interface LearningItemsApi {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Enrol a content item",
            description =
                    """
                    Enrols the content item and creates one card per exercise type and speaker, due immediately. \
                    Enrolling again re-activates an excluded item and creates missing cards; progress is kept.\
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "Enrolled",
                            content = @Content(schema = @Schema(implementation = LearningItemResponse.class))),
                    @ApiResponse(responseCode = "200", description = "Already enrolled, re-activated",
                            content = @Content(schema = @Schema(implementation = LearningItemResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation failed",
                            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Content item not found")
            })
    ResponseEntity<LearningItemResponse> enrol(DatabaseUserJwtAuthenticationToken auth,
                                               @Valid @RequestBody EnrolLearningItemRequest request);

    @DeleteMapping("/{learningItemId}")
    @Operation(summary = "Exclude a learning item from studying; progress is kept", responses = {
            @ApiResponse(responseCode = "204", description = "Excluded"),
            @ApiResponse(responseCode = "404", description = "Learning item not found")
    })
    ResponseEntity<Void> exclude(DatabaseUserJwtAuthenticationToken auth, @PathVariable("learningItemId") UUID learningItemId);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Find the active learning item of a content item", responses = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = LearningItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not enrolled or excluded")
    })
    LearningItemResponse findByContent(DatabaseUserJwtAuthenticationToken auth,
                                       @RequestParam("contentItemId") UUID contentItemId,
                                       @RequestParam("contentItemType") ContentItemType contentItemType);

    @GetMapping(value = "/{learningItemId}/cards", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the spaced repetition cards of a learning item", responses = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = LearningCardsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Learning item not found")
    })
    LearningCardsResponse getCards(DatabaseUserJwtAuthenticationToken auth, @PathVariable("learningItemId") UUID learningItemId);
}
