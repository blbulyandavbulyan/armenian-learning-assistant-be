package com.blbulyandavbulyan.larm.phrase;

import com.blbulyandavbulyan.larm.phrase.internal.PhraseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseService {
    private final PhraseRepository phraseRepository;


}
