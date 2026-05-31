package com.blbulyandavbulyan.larm.phrase;

import java.util.List;

public record BatchSavePhrasesParameters(List<SavePhraseParameters> phraseResources) {
    public List<String> phrases() {
        return phraseResources.stream().map(SavePhraseParameters::phrase).toList();
    }

}
