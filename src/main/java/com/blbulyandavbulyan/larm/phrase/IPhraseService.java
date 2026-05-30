package com.blbulyandavbulyan.larm.phrase;

import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesResult;
import org.springframework.transaction.annotation.Transactional;

public interface IPhraseService {
    @Transactional
    BatchSavePhrasesResult batchSavePhrases(BatchSavePhrasesParameters parameters);
}
