package com.blbulyandavbulyan.larm.dao.repository;

import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.Media;
import org.springframework.data.repository.CrudRepository;

public interface MediaRepository extends CrudRepository<Media, UUID> {
}
