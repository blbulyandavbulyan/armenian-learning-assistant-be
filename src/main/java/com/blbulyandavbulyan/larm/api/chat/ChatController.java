package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.ai.DraftPhraseResource;
import com.blbulyandavbulyan.larm.ai.PhrasesChatService;
import com.blbulyandavbulyan.larm.ai.StructuredPhrasesResource;
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

    private static PhraseResponse mapPhrase(DraftPhraseResource p) {
        return PhraseResponse.builder()
                .phrase(p.phrase())
                .transcription(p.transcription())
                .translations(p.translations().stream().map(ChatController::mapTranslation).toList())
                .build();
    }

    private static PhraseResponse.TranslationResponse mapTranslation(DraftPhraseResource.DraftTranslationResource t) {
        return PhraseResponse.TranslationResponse.builder().translationText(t.translationText()).iso2LanguageCode(t.iso2LanguageCode()).build();
    }
}
