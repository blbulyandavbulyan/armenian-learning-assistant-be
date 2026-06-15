package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.ai.StructuredDialogueResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftPhraseResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResource;
import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;
import org.springframework.stereotype.Component;

@Component
class ChatMapper {

    public PhraseChatResponse mapToChatResponse(StructuredPhrasesResource structuredPhrasesResource) {
        return PhraseChatResponse.builder()
                .message(structuredPhrasesResource.message())
                .phrases(structuredPhrasesResource.phrases().stream().map(this::mapPhrase).toList())
                .build();
    }

    private DraftPhraseResponse mapPhrase(DraftPhraseResource p) {
        return DraftPhraseResponse.builder()
                .phrase(p.phrase())
                .isoLanguageCode(p.isoLanguageCode())
                .transcription(p.transcription())
                .translations(p.translations().stream().map(this::mapTranslation).toList())
                .build();
    }

    private DraftPhraseResponse.TranslationResponse mapTranslation(DraftTranslationResource t) {
        return DraftPhraseResponse.TranslationResponse.builder()
                .translationText(t.translationText())
                .isoLanguageCode(t.isoLanguageCode())
                .build();
    }

    public DialogueChatResponse mapToDialogueResponse(StructuredDialogueResource structuredDialogueResource) {
        return DialogueChatResponse.builder()
                .message(structuredDialogueResource.message())
                .info(mapDialogueInfo(structuredDialogueResource.info()))
                .speakers(structuredDialogueResource.speakers().stream().map(this::mapSpeaker).toList())
                .dialoguePhrases(structuredDialogueResource.dialoguePhrases().stream().map(this::mapDialoguePhrase).toList())
                .build();
    }

    private DialogueChatResponse.DialogueTitleResponse mapDialogueInfo(StructuredDialogueResource.DialogueTitleResource resource) {
        return DialogueChatResponse.DialogueTitleResponse.builder()
                .title(resource.title())
                .transcription(resource.transcription())
                .translations(resource.translations().stream().map(this::mapDialogueTranslationResponse).toList())
                .build();
    }

    private DialogueChatResponse.DialogueTitleResponse.TranslationResponse mapDialogueTranslationResponse(DraftTranslationResource t) {
        return DialogueChatResponse.DialogueTitleResponse.TranslationResponse.builder()
                .translationText(t.translationText())
                .isoLanguageCode(t.isoLanguageCode())
                .build();
    }

    private DialoguePhraseResponse mapDialoguePhrase(StructuredDialogueResource.DialoguePhrase p) {
        return DialoguePhraseResponse.builder()
                .phrase(mapPhrase(p.phrase()))
                .speakerId(p.speakerId())
                .build();
    }

    private SpeakerResponse mapSpeaker(StructuredDialogueResource.SpeakerResource speaker) {
        return SpeakerResponse.builder()
                .id(speaker.id())
                .title(speaker.title())
                .transcription(speaker.transcription())
                .translations(speaker.translations().stream().map(this::mapSpeakerTranslation).toList())
                .build();
    }

    private SpeakerResponse.TranslationResponse mapSpeakerTranslation(DraftTranslationResource t) {
        return SpeakerResponse.TranslationResponse.builder()
                .translationText(t.translationText())
                .isoLanguageCode(t.isoLanguageCode())
                .build();
    }
}
