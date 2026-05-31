package com.blbulyandavbulyan.larm.phrase.service;

import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.IPhraseMediaService;
import com.blbulyandavbulyan.larm.phrase.MediaResource;
import com.blbulyandavbulyan.larm.phrase.dao.Media;
import com.blbulyandavbulyan.larm.phrase.dao.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
class PhraseMediaService implements IPhraseMediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    public List<MediaResource> saveMedias(List<CreateMediaResource> createMediaResources) {
        Iterable<Media> iterable = mediaRepository.saveAll(createMediaResources.stream().map(mediaMapper::toMedia).toList());

        return StreamSupport.stream(iterable.spliterator(), true)
                .map(mediaMapper::fromMedia)
                .toList();
    }
}
