package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.DialogueSpeaker;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface DialogueSpeakerRepository extends Repository<DialogueSpeaker, UUID> {

    @Query("SELECT s.id FROM DialogueSpeaker s WHERE s.dialogue.id = :dialogueId ORDER BY s.createdAt ASC, s.id ASC")
    List<UUID> findIdsByDialogueId(@Param("dialogueId") UUID dialogueId);
}
