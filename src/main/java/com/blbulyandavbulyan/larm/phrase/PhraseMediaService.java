package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhraseMediaService {
    List<MediaResource> saveMedias(List<CreateMediaResource> createMediaResources);

    Optional<MediaResource> findById(UUID mediaId);
}
