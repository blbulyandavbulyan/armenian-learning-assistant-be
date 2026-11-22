package com.blbulyandavbulyan.larm.api.openapi;

/**
 * Holds constants used across OpenAPI annotations to ensure consistency
 * in schema names, descriptions, examples, and property names.
 */
public interface OpenApiConstants {

    /**
     * Holds descriptions for open api, which are used in {@link io.swagger.v3.oas.annotations.media.Schema}.
     */
    @SuppressWarnings("java:S1214")
    interface Descriptions {

        String CHAT_REQUEST_MESSAGE = "Request message, to tell the AI what to do";
        String CHAT_ID = "Id of the conversation (chat), to preserve the memory in context";
        String ISO_LANGUAGE_CODE = "ISO 639-1 language code";
        String GENERATED_PHRASE = "Generated phrase";
        String GENERATED_TRANSCRIPTION = "Generated transcription (in English letters)";
        String GENERATED_TRANSLATION_TEXT = "Generated translation text";
        String CHAT_RESPONSE_MESSAGE = "Response message (description of the results) from AI model";
        String APPROVED_TRANSLATION_TEXT = "Approved translation text";
        String APPROVED_TRANSCRIPTION = "Approved transcription";
        String APPROVED_PHRASE = "Approved phrase";
        String PHRASE_ID = "Id of the phrase";
        String CONTENT_TYPE = "Content type";
        String ASSET_URL = "Asset URL";
        String TRANSLATION_ID = "Translation ID";
        String TIMESTAMP_OF_THE_ERROR = "Timestamp of the error";
        String HTTP_STATUS_CODE = "HTTP status code";
        String ERROR_TYPE_SUMMARY = "Error type summary";
        String ERROR_PATH = "The path where the error occurred";
        String ERROR_MESSAGES_MAP = "Map of field names to their validation error messages";
        String SPEAKER_TITLE = "Speaker title";
        String DIALOGUE_TITLE = "Dialogue title";
        String SPEAKER_ID = "Identifier linking a phrase to a specific speaker";
        String DIALOGUE_TITLE_INFO = "Dialogue title information";
        String DIALOGUE_ID = "The UUID of the saved dialogue";
        String SPEAKERS_LIST = "List of speakers in the dialogue";
        String DIALOGUE_PHRASES = "List of phrases spoken in the dialogue in chronological order";
        String DIALOGUE_PHRASE_ID = "Id of this line of the dialogue (dialogue phrase), used as sub-item id in exercises";
        String CONTENT_ITEM_ID = "Id of the learnable content item, e.g. the dialogue id";
        String CONTENT_ITEM_TYPE = "Type of the content item";
        String LEARNING_ITEM_ID = "Id of the user's enrolment of a content item";
        String LEARNING_CARD_ID = "Id of the spaced repetition card (one per exercise type and variant)";
        String ENROLLED_AT = "When the user first enrolled the content item";
        String EXCLUDED_AT = "When the user excluded the item from studying; null while active";
        String EXERCISE_TYPE = "Type of exercise, defines how answers are given and evaluated";
        String VARIANT_ID = "Part of the content practised separately; for dialogues the id of the speaker the user plays";
        String NEXT_REVIEW_AT = "When the card is due for its next review";
        String INTERVAL_DAYS = "Current review interval in days";
        String REPETITION_COUNT = "Number of successful reviews in a row";
        String EASE_FACTOR = "SM-2 ease factor (minimum 1.3); higher means intervals grow faster";
        String SESSION_ID = "Id of the exercise session";
        String SUB_ITEM_ID = "Id of the sub-item being answered; for dialogues the dialogue phrase id";
        String SUB_ITEM_IDS = "Sub-items the user has to answer in this session, in order";
        String PENDING_SUB_ITEM_IDS = "Sub-items of the session without any attempt yet";
        String STARTED_AT = "When the session was started";
        String COMPLETED_AT = "When the session was completed; null while in progress";
        String USED_HINT = "Whether the user looked at a hint; lowers the quality by one";
        String ANSWER = "The answer; the type field selects the kind of answer";
        String ANSWER_TYPE = "Kind of answer";
        String ANSWER_TEXT = "The typed text";
        String ATTEMPT_ID = "Id of the attempt";
        String QUALITY = "SM-2 quality 0-5 after the hint penalty";
        String CORRECT = "Whether the answer matched the expected answer exactly (independent of hints)";
        String EXPECTED_TEXT = "Correct answer, only present when the answer was not correct";
        String EVALUATED_AT = "When the attempt was evaluated";
        String AGGREGATE_SCORE = "Session score 0-100";
        String PREVIOUS_INTERVAL_DAYS = "Review interval in days before this session";
        String NEXT_INTERVAL_DAYS = "Review interval in days after this session";
        String PAGE = "Zero-based page index";
        String PAGE_SIZE = "Page size";
        String TOTAL_ELEMENTS = "Total number of matching elements";
    }

    /**
     * Holds examples which are used in {@link io.swagger.v3.oas.annotations.media.Schema} and in {@link OpenApiConfiguration}.
     */
    @SuppressWarnings("java:S1214")
    interface Examples {

        String CHAT_REQUEST_MESSAGE = "Hello, could you please generate phrases for going into groceries store?";
        String CHAT_ID = "5d9e3e04-c848-44a0-8d25-103aaecf1856";
        String PHRASE = "Որտե՞ղ է հացի բաժինը:";
        String TRANSCRIPTION = "Vortegh e hatsi bazhiny?";
        String TRANSLATION_ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Где находится отдел хлеба?";
        String CHAT_RESPONSE_MESSAGE = "Here are the phrases for grocery store";
        String PHRASE_ID = "54c6f12c-4201-48fc-80e4-299bae5b75db";
        String CONTENT_TYPE = "audio/wav";
        String ASSET_URL = "http://localhost:8080/assets/c9b251f7-0824-4675-854d-354c063f8045";
        String TRANSLATION_ID = "1ae940f2-1798-4b64-8f04-2f845df8dfe5";
        String PHRASE_ISO_LANGUAGE_CODE = "hy";
        String TIMESTAMP_OF_THE_ERROR = "2023-10-27T10:00:00";
        String HTTP_STATUS_CODE = "400";
        String ERROR_TYPE_SUMMARY = "Validation Failed";
        @SuppressWarnings("java:S1075") // don't care, this is for open api docs
        String ERROR_PATH = "/phrases";
        String SPEAKER_TITLE = "Գնորդ";
        String SPEAKER_TRANSCRIPTION = "Gnord";
        String SPEAKER_TRANSLATION_TEXT = "Покупатель";
        String DIALOGUE_TITLE = "Խանութում";
        String DIALOGUE_TRANSCRIPTION = "Khanutum";
        String SPEAKER_ID = "519f4266-17c2-46fc-a761-f0ef0021e4f1";
        String DIALOGUE_TRANSLATION_TEXT = "В магазине";
        String DIALOGUE_ID = "831a5d64-74c5-4a1e-989e-ab1c310c17f2";
    }

    /**
     * Holds schema names, which are used in {@link io.swagger.v3.oas.annotations.media.Schema}.
     * Constans should be put here only if they are used in {@link OpenApiConfiguration}.
     */
    @SuppressWarnings("java:S1214")
    interface SchemaNames {
        String DIALOGUE_TITLE_RESPONSE = "Dialogue Title Response";
        String SPEAKER_RESPONSE = "Speaker Response";
        String DRAFT_PHRASES_RESPONSE = "Draft Phrases Response";
        String SAVE_DIALOGUE_TITLE_REQUEST = "Save Dialogue Title Request";
        String SAVE_SPEAKER_REQUEST = "Save Speaker Request";
        String SAVE_DIALOGUE_PHRASE_INNER_REQUEST = "Save Dialogue Phrase Inner Request";
    }

    /**
     * Holds property names which are used in {@link OpenApiConfiguration}.
     */
    @SuppressWarnings("java:S1214")
    interface PropertyNames {
        String TRANSLATIONS = "translations";
    }
}
