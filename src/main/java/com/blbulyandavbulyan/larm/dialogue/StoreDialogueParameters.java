package com.blbulyandavbulyan.larm.dialogue;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import lombok.Builder;

@Builder
// TODO sonar warning java:S6218
public record StoreDialogueParameters(
        float[] embedding,
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
