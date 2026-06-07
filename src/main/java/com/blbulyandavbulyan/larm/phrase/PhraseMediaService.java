package com.blbulyandavbulyan.larm.phrase;

import java.util.Optional;
import java.util.UUID;

public interface PhraseMediaService {

    Optional<MediaResource> findById(UUID mediaId);
}
