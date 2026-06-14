package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.api.advice.ValidationErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/chat")
@Tag(name = "Chat", description = "Endpoints for interacting with AI to generate dialoguePhrases and content")
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Validation failed",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ValidationErrorResponse.class)
                )
        )
})
interface ChatApi {
    @PostMapping(value = "/phrases", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Generate phrases using AI",
            description =
                    """
                    Sends a user message and a chat ID to the AI model to generate a set of Armenian phrases, \
                    their transcriptions, and translations based on the provided context.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully generated phrases and descriptive message",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PhraseChatResponse.class)
                            )
                    )
            }
    )
    PhraseChatResponse phrasesChat(@RequestBody ChatRequest request);

    @PostMapping(value = "/dialogue", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Generate a structured dialogue using AI",
            description =
                    """
                    Sends a user message and a chat ID to the AI model to generate a structured Armenian dialogue, \
                    including speakers, their phrases, transcriptions, and translations based on the provided context.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully generated dialogue and descriptive message",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DialogueChatResponse.class)
                            )
                    )
            }
    )
    DialogueChatResponse dialogueChat(@RequestBody ChatRequest request);
}
