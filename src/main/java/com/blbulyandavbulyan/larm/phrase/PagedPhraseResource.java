package com.blbulyandavbulyan.larm.phrase;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.dao.Phrase;
import lombok.Builder;

@Builder
public record PagedPhraseResource(List<Phrase> phrases, Page page) {

    @Builder
    public record Page(int pageNumber, int pageSize, int totalPages) {
    }
}
