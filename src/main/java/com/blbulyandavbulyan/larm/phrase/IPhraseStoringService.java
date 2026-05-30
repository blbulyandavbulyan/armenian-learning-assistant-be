package com.blbulyandavbulyan.larm.phrase;

import org.springframework.transaction.annotation.Transactional;

public interface IPhraseStoringService {
    @Transactional
    BatchSavePhrasesResult batchSavePhrases(BatchSavePhrasesParameters parameters);

    PagedPhraseResource findAll(PageParameters build);
}
