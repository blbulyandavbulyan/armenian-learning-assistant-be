package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Phrases Paged Response")
@Builder
record PhrasesPagedResponse(List<PhraseResponse> phrases, PageResponse page) {
}
