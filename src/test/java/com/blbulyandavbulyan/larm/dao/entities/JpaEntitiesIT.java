package com.blbulyandavbulyan.larm.dao.entities;

import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test class is intentionally written to prevent stupid and dumb bugs in JPA entities,
 * {@link org.hibernate.LazyInitializationException}, {@link StackOverflowError} and so on.
 * The {@link Transactional} with propogation = NOT_SUPPORTED here is intentionally,
 *  so that if we add it for some unknown reason on the parent class,
 *  this test won't become useless, which passes no matter what.
 **/
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Sql(scripts = {
        "/sql-test-scripts/drop-all-data-after-test.sql",
        "/sql-test-scripts/insert-dialogue.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JpaEntitiesIT extends BaseIT {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void dialogueToString() {
        Dialogue dialogue = transactionTemplate.execute(_ ->
                entityManager.find(Dialogue.class, UUID.fromString("99999999-9999-9999-9999-999999999999")));
        
        assertThatCode(() -> {
            String dialogueString = dialogue.toString();
            assertThat(dialogueString)
                    .contains("Dialogue{id=99999999-9999-9999-9999-999999999999")
                    .contains("(LAZY LOADING)");
        }).doesNotThrowAnyException();
    }

    @Test
    void phraseToString() {
        Phrase phrase = transactionTemplate.execute(_ ->
                entityManager.find(Phrase.class, UUID.fromString("11111111-1111-1111-1111-111111111111")));
                
        assertThatCode(() -> {
            String phraseString = phrase.toString();
            assertThat(phraseString)
                    .contains("Phrase{id=11111111-1111-1111-1111-111111111111")
                    .contains("(LAZY LOADING)");
        }).doesNotThrowAnyException();
    }

    @Test
    void mediaToString() {
        Media media = transactionTemplate.execute(_ ->
                entityManager.find(Media.class, UUID.fromString("88888888-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            String mediaString = media.toString();
            assertThat(mediaString)
                    .contains("Media{id=88888888-1111-1111-1111-111111111111")
                    .contains("(LAZY LOADING)");
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueSpeakerToString() {
        DialogueSpeaker dialogueSpeaker = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeaker.class, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));

        assertThatCode(() -> {
            String speakerString = dialogueSpeaker.toString();
            assertThat(speakerString)
                    .contains("DialogueSpeaker{id=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                    .contains("(LAZY LOADING)");
        }).doesNotThrowAnyException();
    }

    @Test
    void dialoguePhraseToString() {
        DialoguePhrase dialoguePhrase = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhrase.class, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));

        assertThatCode(() -> {
            String dialoguePhraseString = dialoguePhrase.toString();
            assertThat(dialoguePhraseString)
                    .contains("DialoguePhrase{id=eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
                    .contains("(LAZY LOADING)");
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueTitleTranslationToString() {
        DialogueTitleTranslation titleTranslation = transactionTemplate.execute(_ ->
                entityManager.find(DialogueTitleTranslation.class, UUID.fromString("77777777-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            String titleTranslationString = titleTranslation.toString();
            assertThat(titleTranslationString)
                    .contains("DialogueTitleTranslation{id=77777777-1111-1111-1111-111111111111");
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueSpeakerTranslationToString() {
        DialogueSpeakerTranslation speakerTranslation = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeakerTranslation.class, UUID.fromString("77777777-2222-2222-2222-222222222222")));

        assertThatCode(() -> {
            String speakerTranslationString = speakerTranslation.toString();
            assertThat(speakerTranslationString)
                    .contains("DialogueSpeakerTranslation{id=77777777-2222-2222-2222-222222222222");
        }).doesNotThrowAnyException();
    }

    @Test
    void dialoguePhraseTranslationToString() {
        DialoguePhraseTranslation phraseTranslation = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhraseTranslation.class, UUID.fromString("77777777-4444-4444-4444-444444444444")));

        assertThatCode(() -> {
            String phraseTranslationString = phraseTranslation.toString();
            assertThat(phraseTranslationString)
                    .contains("DialoguePhraseTranslation{id=77777777-4444-4444-4444-444444444444");
        }).doesNotThrowAnyException();
    }
}
