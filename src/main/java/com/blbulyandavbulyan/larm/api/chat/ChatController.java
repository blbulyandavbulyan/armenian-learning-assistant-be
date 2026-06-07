package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.ai.PhrasesChatService;
import com.blbulyandavbulyan.larm.ai.chat.DraftPhraseResource;
import com.blbulyandavbulyan.larm.ai.chat.StructuredPhrasesResource;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
class ChatController {
    private final PhrasesChatService phrasesChatService;

    @PostMapping("/phrases")
    public PhraseChatResponse chat(@RequestBody ChatRequest request) {
        final StructuredPhrasesResource structuredPhrasesResource = phrasesChatService.chat(request.message(), request.chatId());

        return PhraseChatResponse.builder()
                .message(structuredPhrasesResource.message())
                .phrases(structuredPhrasesResource.phrases().stream().map(ChatController::mapPhrase).toList())
                .build();
    }

    private static DraftPhraseResponse mapPhrase(DraftPhraseResource p) {
        return DraftPhraseResponse.builder()
                .phrase(p.phrase())
                .transcription(p.transcription())
                .translations(p.translations().stream().map(ChatController::mapTranslation).toList())
                .build();
    }

    private static DraftPhraseResponse.TranslationResponse mapTranslation(DraftPhraseResource.DraftTranslationResource t) {
        return DraftPhraseResponse.TranslationResponse.builder().translationText(t.translationText()).isoLanguageCode(t.isoLanguageCode()).build();
    }
}
