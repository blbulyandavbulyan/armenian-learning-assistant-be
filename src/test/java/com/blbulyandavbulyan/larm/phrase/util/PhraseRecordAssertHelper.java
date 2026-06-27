package com.blbulyandavbulyan.larm.phrase.util;

import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.TestPhraseRepository;
import com.blbulyandavbulyan.larm.phrase.dao.projections.PhraseRecord;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.OptionalAssert;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class PhraseRecordAssertHelper {

    private final TestPhraseRepository testPhraseRepository;

    public OptionalAssert<PhraseRecord> assertThatPhraseWithId(UUID phraseId) {
        return Assertions.assertThat(testPhraseRepository.findById(phraseId).map(PhraseRecord::new));
    }

}
