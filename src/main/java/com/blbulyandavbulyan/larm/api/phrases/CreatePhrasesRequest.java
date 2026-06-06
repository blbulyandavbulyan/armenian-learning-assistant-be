package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record CreatePhrasesRequest(@NotEmpty List<@NotNull CreatePhraseRequest> phrases) {
}
