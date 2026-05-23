package com.blbulyandavbulyan.larm.phrase.internal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AudioFileRepository extends CrudRepository<AudioFile, UUID> {
}
