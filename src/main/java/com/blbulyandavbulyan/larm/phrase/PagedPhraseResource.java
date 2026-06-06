package com.blbulyandavbulyan.larm.phrase;

import java.util.List;

import lombok.Builder;

@Builder
public record PagedPhraseResource(List<PhraseResource> phrases, Page page) {

    @Builder
    public record Page(int pageNumber, int pageSize, int totalPages) {
    }
}
