package com.blbulyandavbulyan.larm.phrase.service;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.MediaResource;
import com.blbulyandavbulyan.larm.phrase.PhraseMediaService;
import com.blbulyandavbulyan.larm.phrase.dao.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class DefaultPhraseMediaService implements PhraseMediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    public Optional<MediaResource> findById(UUID mediaId) {
        return mediaRepository.findById(mediaId)
                .map(mediaMapper::fromMedia);
    }

}
