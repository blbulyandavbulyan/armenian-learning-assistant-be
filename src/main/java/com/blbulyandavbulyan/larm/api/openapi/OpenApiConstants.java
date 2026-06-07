package com.blbulyandavbulyan.larm.api.openapi;

public interface OpenApiConstants {
    interface Descriptions {

        String CHAT_REQUEST_MESSAGE = "Request message, to tell the AI what to do";
        String CHAT_ID = "Id of the conversation (chat), to preserve the memory in context";
        String ISO_LANGUAGE_CODE = "ISO 639-1 language code";
        String GENERATED_PHRASE = "Generated phrase";
        String GENERATED_TRANSCRIPTION = "Generated transcription (in English letters)";
        String GENERATED_TRANSLATION_TEXT = "Generated translation text";
        String PHRASE_CHAT_RESPONSE_MESSAGE = "Response message (description of the results) from AI model";
        String APPROVED_TRANSLATION_TEXT = "Approved translation text";
        String APPROVED_TRANSCRIPTION = "Approved transcription";
        String APPROVED_PHRASE = "Approved phrase";
        String PHRAE_ID = "Id of the phrase";
        String CONTENT_TYPE = "Content type";
        String ASSET_URL = "Asset URL";
        String TRANSLATION_ID = "Translation ID";
        String PAGE_NUMBER = "Page number (starting from 1)";
        String PAGE_SIZE = "Page size in [10; 100]";
        String TOTAL_PAGES = "Total pages";
    }

    interface Examples {

        String CHAT_REQUEST_MESSAGE = "Hello, could you please generate phrases for going into groceries store?";
        String CHAT_ID = "5d9e3e04-c848-44a0-8d25-103aaecf1856";
        String PHRASE = "Որտե՞ղ է հացի բաժինը:";
        String TRANSCRIPTION = "Vortegh e hatsi bazhiny?";
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Где находится отдел хлеба?";
        String PHRASE_CHAT_RESPONSE_MESSAGE = "Here are the phrases for grocery store";
        String PHRASE_ID = "54c6f12c-4201-48fc-80e4-299bae5b75db";
        String CONTENT_TYPE = "audio/wav";
        String ASSET_URL = "http://localhost:8080/assets/c9b251f7-0824-4675-854d-354c063f8045";
        String TRANSLATION_ID = "1ae940f2-1798-4b64-8f04-2f845df8dfe5";
        String PAGE_NUMBER = "1";
        String PAGE_SIZE = "10";
        String TOTAL_PAGES = "5";
    }
}
