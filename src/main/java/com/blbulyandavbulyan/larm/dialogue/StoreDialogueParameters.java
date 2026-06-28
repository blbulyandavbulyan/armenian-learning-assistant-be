package com.blbulyandavbulyan.larm.dialogue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

@Builder
public record StoreDialogueParameters(
        float[] embedding,
        SavePhraseParameters titlePhrase,
        List<StoreSpeakerParameters> speakers,
        List<StoreDialoguePhraseParameters> dialoguePhrases) {

    @SuppressWarnings({"DeconstructionCanBeUsed", "java:S6878"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StoreDialogueParameters that)) {
            return false;
        }
        return Arrays.equals(this.embedding, that.embedding) &&
                Objects.equals(this.titlePhrase, that.titlePhrase) &&
                Objects.equals(this.speakers, that.speakers) &&
                Objects.equals(this.dialoguePhrases, that.dialoguePhrases);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(titlePhrase, speakers, dialoguePhrases);
        result = 31 * result + Arrays.hashCode(embedding);
        return result;
    }

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

    @Override
    @NonNull
    public String toString() {
        return "StoreDialogueParameters{embedding=%s, titlePhrase=%s, speakers=%s, dialoguePhrases=%s}"
                .formatted(Arrays.toString(embedding), titlePhrase, speakers, dialoguePhrases);
    }
}
