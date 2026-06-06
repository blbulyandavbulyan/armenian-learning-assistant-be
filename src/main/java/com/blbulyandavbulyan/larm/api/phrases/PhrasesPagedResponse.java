package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import lombok.Builder;

@Builder
record PhrasesPagedResponse(List<PhraseResponse> phrases, PageResponse page) {
}
