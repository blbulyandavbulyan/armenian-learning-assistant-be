package com.blbulyandavbulyan.larm.phrase.service;

import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record BatchSavePhrasesResult(Set<String> existingPhrases, List<PhraseResource> savedPhrases) {
}
