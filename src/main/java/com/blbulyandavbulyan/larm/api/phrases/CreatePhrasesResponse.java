package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Create Phrases Response")
public record CreatePhrasesResponse(List<PhraseResponse> phrases) {
}
