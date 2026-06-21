package com.blbulyandavbulyan.larm.phrase.service;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.PhraseMediaService;
import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dao.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class DefaultPhraseMediaService implements PhraseMediaService {
    private final MediaRepository mediaRepository;

    @Override
    public Optional<Media> findById(UUID mediaId) {
        return mediaRepository.findById(mediaId);
    }

}
