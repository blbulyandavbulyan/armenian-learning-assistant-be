package com.blbulyandavbulyan.larm.dao.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.DialoguePhrase;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface DialoguePhraseRepository extends Repository<DialoguePhrase, UUID> {

    @Query("SELECT dp.id FROM DialoguePhrase dp WHERE dp.dialogue.id = :dialogueId ORDER BY dp.orderIndex ASC")
    List<UUID> findIdsByDialogueId(@Param("dialogueId") UUID dialogueId);

    @Query(
            """
            SELECT dp.id FROM DialoguePhrase dp
            WHERE dp.dialogue.id = :dialogueId AND dp.speaker.id = :speakerId
            ORDER BY dp.orderIndex ASC
            """)
    List<UUID> findIdsByDialogueIdAndSpeakerId(@Param("dialogueId") UUID dialogueId, @Param("speakerId") UUID speakerId);

    @Query(
            """
            SELECT p.phrase AS text, p.transcription AS transcription
            FROM DialoguePhrase dp JOIN dp.phrase p
            WHERE dp.id = :id AND dp.dialogue.id = :dialogueId
            """)
    Optional<PhraseTextView> findPhraseText(@Param("dialogueId") UUID dialogueId, @Param("id") UUID id);

    /**
     * Read-only projection of the text of a dialogue line.
     */
    interface PhraseTextView {
        String getText();

        @Nullable
        String getTranscription();
    }
}
