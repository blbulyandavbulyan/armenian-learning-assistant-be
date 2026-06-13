package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Create Phrases Request")
record CreatePhrasesRequest(@NotEmpty List<@NotNull @Valid CreatePhraseRequest> phrases) {
}
