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
     * @throws PhrasesAlreadyExistException if at least some of the phrases are already saved, this fails the entire saving operation
     * @throws InvalidIsoLanguageCodeException in case if some translations have invalid iso language code
     */
    @Transactional
    List<Phrase> batchSavePhrases(BatchSavePhrasesParameters parameters);
}
