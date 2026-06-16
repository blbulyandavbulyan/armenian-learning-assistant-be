package com.blbulyandavbulyan.larm.api.openapi;

public interface OpenApiConstants {
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
        String PAGE_NUMBER = "Page number (starting from 1)";
        String PAGE_SIZE = "Page size in [10; 100]";
        String TOTAL_PAGES = "Total pages";
        String TIMESTAMP_OF_THE_ERROR = "Timestamp of the error";
        String HTTP_STATUS_CODE = "HTTP status code";
        String ERROR_TYPE_SUMMARY = "Error type summary";
        String ERROR_PATH = "The path where the error occurred";
        String ERROR_MESSAGES_MAP = "Map of field names to their validation error messages";
        String SPEAKER_TITLE = "Speaker title";
        String DIALOGUE_TITLE = "Dialogue title";
        String SPEAKER_ID = "Identifier linking a phrase to a specific speaker";
    }

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
        String PAGE_NUMBER = "1";
        String PAGE_SIZE = "10";
        String TOTAL_PAGES = "5";
        String PHRASE_ISO_LANGUAGE_CODE = "hy";
        String TIMESTAMP_OF_THE_ERROR = "2023-10-27T10:00:00";
        String HTTP_STATUS_CODE = "400";
        String ERROR_TYPE_SUMMARY = "Validation Failed";
        String ERROR_PATH = "/phrases";
        String SPEAKER_TITLE = "Գնորդ";
        String SPEAKER_TRANSCRIPTION = "Gnord";
        String SPEAKER_TRANSLATION_TEXT = "Покупатель";
        String DIALOGUE_TITLE = "Խանութում";
        String DIALOGUE_TRANSCRIPTION = "Khanutum";
        String SPEAKER_ID = "speaker-1";
        String DIALOGUE_TRANSLATION_TEXT = "В магазине";
    }

    interface SchemaNames {
        String DIALOGUE_TITLE_RESPONSE = "Dialogue Title Response";
        String SPEAKER_RESPONSE = "Speaker Response";
        String DRAFT_PHRASES_RESPONSE = "Draft Phrases Response";
    }

    interface PropertyNames {
        String TRANSLATIONS = "translations";
    }
}
