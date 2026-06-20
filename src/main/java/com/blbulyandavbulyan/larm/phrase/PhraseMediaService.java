package com.blbulyandavbulyan.larm.phrase;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.Media;

public interface PhraseMediaService {

    Optional<Media> findById(UUID mediaId);
}
