package com.blbulyandavbulyan.larm.phrase;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IPhraseStoringService {
    /**
     * Saved phrases in batch
     * @param parameters for saving phrase
     * @return list of saved phrases
     * @throws PhrasesAlreadyExistException if at least some of the phrases are already saved, this fails the entire saving operation
     * @throws InvalidIsoLanguageCodeException in case if some translations have invalid iso language code
     */
    @Transactional
    List<PhraseResource> batchSavePhrases(BatchSavePhrasesParameters parameters);

    /**
     * Finds all phrases by page parameters
     * @param pageParameters sets the expected page and size
     * @return phrases corresponding to the given page
     */
    PagedPhraseResource findAll(PageParameters pageParameters);
}
