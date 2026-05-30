package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

import java.util.List;

@Builder
public record PagedPhraseResource(List<PhraseResource> phrases, Page page) {

    @Builder
    public record Page(int pageNumber, int pageSize, int totalPages) {
    }
}
