package com.blbulyandavbulyan.larm.phrase;

import java.util.List;

import com.blbulyandavbulyan.larm.dao.entities.Phrase;
import org.springframework.transaction.annotation.Transactional;

public interface PhraseStoringService {
    /**
     * Saved phrases in batch.
     *
     * @param parameters for saving phrase
     * @return list of saved phrases
     */
    @Transactional
    List<Phrase> batchSavePhrases(BatchSavePhrasesParameters parameters);
}
