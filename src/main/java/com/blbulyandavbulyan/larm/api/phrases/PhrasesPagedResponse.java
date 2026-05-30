package com.blbulyandavbulyan.larm.api.phrases;

import lombok.Builder;

import java.util.List;

@Builder
record PhrasesPagedResponse(List<PhraseResponse> phrases, PageResponse page) {
}
