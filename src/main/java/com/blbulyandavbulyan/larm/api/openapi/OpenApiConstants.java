package com.blbulyandavbulyan.larm.api.openapi;

public interface OpenApiConstants {
    interface Descriptions {

        String CHAT_REQUEST_MESSAGE = "Request message, to tell the AI what to do";
        String CHAT_ID = "Id of the conversation (chat), to preserve the memory in context";
        String ISO_LANGUAGE_CODE = "ISO 639-1 language code";
        String GENERATED_PHRASE = "Generated phrase";
        String GENERATED_TRANSCRIPTION = "Generated transcription (in English letters)";
        String GENERATED_TRANSLATION_TEXT = "Generated translation text";
    }

    interface Examples {

        String CHAT_REQUEST_MESSAGE = "Hello, could you please generate phrases for going into groceries store?";
        String CHAT_ID = "5d9e3e04-c848-44a0-8d25-103aaecf1856";
        String PHRASE = "Որտե՞ղ է հացի բաժինը:";
        String TRANSCRIPTION = "Vortegh e hatsi bazhiny?";
        String ISO_LANGUAGE_CODE = "ru";
        String TRANSLATION_TEXT = "Где находится отдел хлеба?";
    }
}
