package com.blbulyandavbulyan.larm.phrase;

import lombok.Getter;

import java.util.Set;

@Getter
public class PhrasesAlreadyExistException extends RuntimeException {
    private final Set<String> alreadySavedPhrases;

    public PhrasesAlreadyExistException(Set<String> alreadySavedPhrases) {
        this.alreadySavedPhrases = alreadySavedPhrases;
        super("The following phrases are already saved: %s".formatted(alreadySavedPhrases));
    }
}
