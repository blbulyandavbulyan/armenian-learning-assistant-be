package com.blbulyandavbulyan.larm.dialogue;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import lombok.Builder;

@Builder
public record StoreDialogueParameters(
        SavePhraseParameters titlePhrase,
        List<StoreSpeakerParameters> speakers,
        List<StoreDialoguePhraseParameters> dialoguePhrases) {

    @Builder
    public record StoreSpeakerParameters(
            String speakerRefId,
            SavePhraseParameters namePhrase) {
    }

    @Builder
    public record StoreDialoguePhraseParameters(
            String speakerRefId,
            SavePhraseParameters phrase) {
    }
}
