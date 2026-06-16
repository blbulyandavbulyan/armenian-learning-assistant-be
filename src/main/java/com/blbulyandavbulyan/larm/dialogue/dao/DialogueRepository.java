package com.blbulyandavbulyan.larm.dialogue.dao;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface DialogueRepository extends CrudRepository<Dialogue, UUID>, DialogueRepositoryCustom {
}
