package com.blbulyandavbulyan.larm.phrase;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Media;
import com.blbulyandavbulyan.larm.dao.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseMediaService {
    private final MediaRepository mediaRepository;

    public Optional<Media> findById(UUID mediaId) {
        return mediaRepository.findById(mediaId);
    }

}
