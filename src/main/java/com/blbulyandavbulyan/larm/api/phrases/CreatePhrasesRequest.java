package com.blbulyandavbulyan.larm.api.phrases;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

record CreatePhrasesRequest(@NotEmpty List<@NotNull CreatePhraseRequest> phrases) {
}
