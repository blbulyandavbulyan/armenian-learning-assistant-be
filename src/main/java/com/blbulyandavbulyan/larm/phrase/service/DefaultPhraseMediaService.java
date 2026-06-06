package com.blbulyandavbulyan.larm.phrase.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.MediaResource;
import com.blbulyandavbulyan.larm.phrase.PhraseMediaService;
import com.blbulyandavbulyan.larm.phrase.dao.Media;
import com.blbulyandavbulyan.larm.phrase.dao.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class DefaultPhraseMediaService implements PhraseMediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    public List<MediaResource> saveMedias(List<CreateMediaResource> createMediaResources) {
        Iterable<Media> iterable = mediaRepository.saveAll(createMediaResources.stream().map(mediaMapper::toMedia).toList());

        return StreamSupport.stream(iterable.spliterator(), true)
                .map(mediaMapper::fromMedia)
                .toList();
    }

    @Override
    public Optional<MediaResource> findById(UUID mediaId) {
        return mediaRepository.findById(mediaId)
                .map(mediaMapper::fromMedia);
    }

}
