package com.blbulyandavbulyan.larm.dialogue.dao;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dialogue.FullDialogueResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;

@Slf4j
public class DialogueRepositoryImpl implements DialogueRepositoryCustom {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public DialogueRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @Override
    public Optional<FullDialogueResource> findFullById(UUID id) {
        //TODO this is probably very dumb and overkill, verify that this complex sql bullsh*t is actually NEEDED here
        // and if this json related stuff can be 'avoided'
        String sql =
                """
                SELECT json_build_object(
                    'id', d.id,
                    'title', (
                        SELECT json_build_object(
                            'id', p.id,
                            'phrase', p.phrase,
                            'isoLanguageCode', p.iso_language_code,
                            'transcription', p.transcription,
                            'translations', COALESCE((SELECT json_agg(json_build_object(
                                'id', t.id,
                                'translationText', t.translation_text,
                                'isoLanguageCode', t.iso_language_code
                            )) FROM translations t WHERE t.phrase_id = p.id), '[]'::json),
                            'media', COALESCE((SELECT json_agg(json_build_object(
                                'id', m.id,
                                'contentType', m.content_type
                            )) FROM medias m WHERE m.phrase_id = p.id), '[]'::json)
                        )
                        FROM phrases p WHERE p.id = d.title_phrase_id
                    ),
                    'speakers', COALESCE((
                        SELECT json_agg(json_build_object(
                            'speakerRefId', ds.speaker_ref_id,
                            'name', (
                                SELECT json_build_object(
                                    'id', p.id,
                                    'phrase', p.phrase,
                                    'isoLanguageCode', p.iso_language_code,
                                    'transcription', p.transcription,
                                    'translations', COALESCE((SELECT json_agg(json_build_object(
                                        'id', t.id,
                                        'translationText', t.translation_text,
                                        'isoLanguageCode', t.iso_language_code
                                    )) FROM translations t WHERE t.phrase_id = p.id), '[]'::json),
                                    'media', COALESCE((SELECT json_agg(json_build_object(
                                        'id', m.id,
                                        'contentType', m.content_type
                                    )) FROM medias m WHERE m.phrase_id = p.id), '[]'::json)
                                )
                                FROM phrases p WHERE p.id = ds.name_phrase_id
                            )
                        ))
                        FROM dialogue_speakers ds WHERE ds.dialogue_id = d.id
                    ), '[]'::json),
                    'dialoguePhrases', COALESCE((
                        SELECT json_agg(json_build_object(
                            'speakerRefId', (SELECT speaker_ref_id FROM dialogue_speakers s WHERE s.id = dp.speaker_id),
                            'phrase', (
                                SELECT json_build_object(
                                    'id', p.id,
                                    'phrase', p.phrase,
                                    'isoLanguageCode', p.iso_language_code,
                                    'transcription', p.transcription,
                                    'translations', COALESCE((SELECT json_agg(json_build_object(
                                        'id', t.id,
                                        'translationText', t.translation_text,
                                        'isoLanguageCode', t.iso_language_code
                                    )) FROM translations t WHERE t.phrase_id = p.id), '[]'::json),
                                    'media', COALESCE((SELECT json_agg(json_build_object(
                                        'id', m.id,
                                        'contentType', m.content_type
                                    )) FROM medias m WHERE m.phrase_id = p.id), '[]'::json)
                                )
                                FROM phrases p WHERE p.id = dp.phrase_id
                            )
                        ) ORDER BY dp.order_index)
                        FROM dialogue_phrases dp WHERE dp.dialogue_id = d.id
                    ), '[]'::json)
                )
                FROM dialogues d
                WHERE d.id = :id
                """;

        return jdbcClient.sql(sql)
                .param("id", id)
                .query(String.class)
                .optional()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, FullDialogueResource.class);
                    } catch (Exception e) {
                        log.error("Failed to parse dialogue JSON", e);
                        throw new RuntimeException("Failed to parse dialogue JSON", e);
                    }
                });
    }
}
