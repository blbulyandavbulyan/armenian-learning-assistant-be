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

    private TranslationResponse mapTranslation(DraftTranslationResource t) {
        return TranslationResponse.builder().translationText(t.translationText()).isoLanguageCode(t.isoLanguageCode()).build();
    }

    public DialogueChatResponse mapToDialogueResponse(StructuredDialogueResource structuredDialogueResource) {
        return DialogueChatResponse.builder()
                .message(structuredDialogueResource.message())
                .dialoguePhrases(structuredDialogueResource.dialoguePhrases().stream().map(this::mapDialoguePhrase).toList())
                .build();
    }

    private DialoguePhraseResponse mapDialoguePhrase(StructuredDialogueResource.DialoguePhrase p) {
        return DialoguePhraseResponse.builder()
                .phrase(mapPhrase(p.phrase()))
                .speaker(mapSpeaker(p.speaker()))
                .build();
    }

    private SpeakersResponse mapSpeaker(StructuredDialogueResource.SpeakerResource speaker) {
        return SpeakersResponse.builder()
                .title(speaker.title())
                .transcription(speaker.transcription())
                .translations(speaker.translations().stream().map(this::mapTranslation).toList())
                .build();
    }
}
