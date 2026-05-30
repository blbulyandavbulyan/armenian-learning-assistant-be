package com.blbulyandavbulyan.larm.api.phrases;

import lombok.Builder;

import java.util.List;

@Builder
record PhrasesResponse(List<PhraseResponse> phrases, PageResponse page) {
}
