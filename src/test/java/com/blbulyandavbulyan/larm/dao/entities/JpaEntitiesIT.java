package com.blbulyandavbulyan.larm.dao.entities;

import java.util.UUID;

import com.blbulyandavbulyan.larm.BaseIT;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.SoftAssertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.blbulyandavbulyan.larm.srs.SrsTestData.CARD_A_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.FIRST_ATTEMPT_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.IN_PROGRESS_SESSION_ID;
import static com.blbulyandavbulyan.larm.srs.SrsTestData.LEARNING_ITEM_ID;
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
        "/sql-test-scripts/insert-dialogue.sql",
        "/sql-test-scripts/srs/insert-test-user.sql",
        "/sql-test-scripts/srs/insert-active-learning-item.sql",
        "/sql-test-scripts/srs/insert-in-progress-session.sql",
        "/sql-test-scripts/srs/insert-first-line-attempts.sql"
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
    void dialogueEquals() {
        Dialogue proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(Dialogue.class, UUID.fromString("99999999-9999-9999-9999-999999999999")));
        Dialogue proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(Dialogue.class, UUID.fromString("99999999-9999-9999-9999-999999999999")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new Dialogue());
            assertThat(new Dialogue()).isNotEqualTo(proxy1);
            assertThat(new Dialogue()).isNotEqualTo(new Dialogue());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueHashCode() {
        Dialogue proxy = transactionTemplate.execute(_ ->
                entityManager.find(Dialogue.class, UUID.fromString("99999999-9999-9999-9999-999999999999")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("Dialogue".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueLazyLoading() {
        Dialogue dialogue = transactionTemplate.execute(_ ->
                entityManager.find(Dialogue.class, UUID.fromString("99999999-9999-9999-9999-999999999999")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(dialogue::getEmbedding)
                    .as("Embedding property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialogue.getTitle().getPhrase())
                    .as("Title property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialogue.getSpeakers().size())
                    .as("Speakers collection must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialogue.getDialoguePhrases().size())
                    .as("DialoguePhrases collection must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialogue.getTitleTranslations().size())
                    .as("TitleTranslations collection must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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
    void phraseEquals() {
        Phrase proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(Phrase.class, UUID.fromString("11111111-1111-1111-1111-111111111111")));
        Phrase proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(Phrase.class, UUID.fromString("11111111-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new Phrase());
            assertThat(new Phrase()).isNotEqualTo(proxy1);
            assertThat(new Phrase()).isNotEqualTo(new Phrase());
        }).doesNotThrowAnyException();
    }

    @Test
    void phraseHashCode() {
        Phrase proxy = transactionTemplate.execute(_ ->
                entityManager.find(Phrase.class, UUID.fromString("11111111-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("Phrase".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void phraseLazyLoading() {
        Phrase phrase = transactionTemplate.execute(_ ->
                entityManager.find(Phrase.class, UUID.fromString("11111111-1111-1111-1111-111111111111")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> phrase.getMediaSet().size())
                    .as("MediaSet collection must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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
    void mediaEquals() {
        Media proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(Media.class, UUID.fromString("88888888-1111-1111-1111-111111111111")));
        Media proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(Media.class, UUID.fromString("88888888-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new Media());
            assertThat(new Media()).isNotEqualTo(proxy1);
            assertThat(new Media()).isNotEqualTo(new Media());
        }).doesNotThrowAnyException();
    }

    @Test
    void mediaHashCode() {
        Media proxy = transactionTemplate.execute(_ ->
                entityManager.find(Media.class, UUID.fromString("88888888-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("Media".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void mediaLazyLoading() {
        Media media = transactionTemplate.execute(_ ->
                entityManager.find(Media.class, UUID.fromString("88888888-1111-1111-1111-111111111111")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> media.getPhrase().getPhrase())
                    .as("Phrase property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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
    void dialogueSpeakerEquals() {
        DialogueSpeaker proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeaker.class, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));
        DialogueSpeaker proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeaker.class, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new DialogueSpeaker());
            assertThat(new DialogueSpeaker()).isNotEqualTo(proxy1);
            assertThat(new DialogueSpeaker()).isNotEqualTo(new DialogueSpeaker());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueSpeakerHashCode() {
        DialogueSpeaker proxy = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeaker.class, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("DialogueSpeaker".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueSpeakerLazyLoading() {
        DialogueSpeaker speaker = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeaker.class, UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> speaker.getDialogue().getCreatedAt())
                    .as("Dialogue property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> speaker.getNamePhrase().getPhrase())
                    .as("NamePhrase property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> speaker.getTranslations().size())
                    .as("Translations collection must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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
    void dialoguePhraseEquals() {
        DialoguePhrase proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhrase.class, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));
        DialoguePhrase proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhrase.class, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new DialoguePhrase());
            assertThat(new DialoguePhrase()).isNotEqualTo(proxy1);
            assertThat(new DialoguePhrase()).isNotEqualTo(new DialoguePhrase());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialoguePhraseHashCode() {
        DialoguePhrase proxy = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhrase.class, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("DialoguePhrase".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialoguePhraseLazyLoading() {
        DialoguePhrase dialoguePhrase = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhrase.class, UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> dialoguePhrase.getDialogue().getCreatedAt())
                    .as("Dialogue property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialoguePhrase.getPhrase().getPhrase())
                    .as("Phrase property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialoguePhrase.getSpeaker().getCreatedAt())
                    .as("Speaker property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
            softly.assertThatThrownBy(() -> dialoguePhrase.getTranslations().size())
                    .as("Translations collection must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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
    void dialogueTitleTranslationEquals() {
        DialogueTitleTranslation proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(DialogueTitleTranslation.class, UUID.fromString("77777777-1111-1111-1111-111111111111")));
        DialogueTitleTranslation proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(DialogueTitleTranslation.class, UUID.fromString("77777777-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new DialogueTitleTranslation());
            assertThat(new DialogueTitleTranslation()).isNotEqualTo(proxy1);
            assertThat(new DialogueTitleTranslation()).isNotEqualTo(new DialogueTitleTranslation());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueTitleTranslationHashCode() {
        DialogueTitleTranslation proxy = transactionTemplate.execute(_ ->
                entityManager.find(DialogueTitleTranslation.class, UUID.fromString("77777777-1111-1111-1111-111111111111")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("DialogueTitleTranslation".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueTitleTranslationLazyLoading() {
        DialogueTitleTranslation translation = transactionTemplate.execute(_ ->
                entityManager.find(DialogueTitleTranslation.class, UUID.fromString("77777777-1111-1111-1111-111111111111")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> translation.getDialogue().getCreatedAt())
                    .as("Dialogue property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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
    void dialogueSpeakerTranslationEquals() {
        DialogueSpeakerTranslation proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeakerTranslation.class, UUID.fromString("77777777-2222-2222-2222-222222222222")));
        DialogueSpeakerTranslation proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeakerTranslation.class, UUID.fromString("77777777-2222-2222-2222-222222222222")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new DialogueSpeakerTranslation());
            assertThat(new DialogueSpeakerTranslation()).isNotEqualTo(proxy1);
            assertThat(new DialogueSpeakerTranslation()).isNotEqualTo(new DialogueSpeakerTranslation());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueSpeakerTranslationHashCode() {
        DialogueSpeakerTranslation proxy = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeakerTranslation.class, UUID.fromString("77777777-2222-2222-2222-222222222222")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("DialogueSpeakerTranslation".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialogueSpeakerTranslationLazyLoading() {
        DialogueSpeakerTranslation translation = transactionTemplate.execute(_ ->
                entityManager.find(DialogueSpeakerTranslation.class, UUID.fromString("77777777-2222-2222-2222-222222222222")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> translation.getDialogueSpeaker().getCreatedAt())
                    .as("DialogueSpeaker property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
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

    @Test
    void dialoguePhraseTranslationEquals() {
        DialoguePhraseTranslation proxy1 = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhraseTranslation.class, UUID.fromString("77777777-4444-4444-4444-444444444444")));
        DialoguePhraseTranslation proxy2 = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhraseTranslation.class, UUID.fromString("77777777-4444-4444-4444-444444444444")));

        assertThatCode(() -> {
            assertThat(proxy1).isEqualTo(proxy2);
            assertThat(proxy1).isNotEqualTo(new DialoguePhraseTranslation());
            assertThat(new DialoguePhraseTranslation()).isNotEqualTo(proxy1);
            assertThat(new DialoguePhraseTranslation()).isNotEqualTo(new DialoguePhraseTranslation());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialoguePhraseTranslationHashCode() {
        DialoguePhraseTranslation proxy = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhraseTranslation.class, UUID.fromString("77777777-4444-4444-4444-444444444444")));

        assertThatCode(() -> {
            int hashCode = proxy.hashCode();
            assertThat(hashCode).isEqualTo("DialoguePhraseTranslation".hashCode());
        }).doesNotThrowAnyException();
    }

    @Test
    void dialoguePhraseTranslationLazyLoading() {
        DialoguePhraseTranslation translation = transactionTemplate.execute(_ ->
                entityManager.find(DialoguePhraseTranslation.class, UUID.fromString("77777777-4444-4444-4444-444444444444")));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> translation.getDialoguePhrase().getOrderIndex())
                    .as("DialoguePhrase property must be LAZY loaded")
                    .isInstanceOf(LazyInitializationException.class);
        });
    }

    @Test
    void learningItemToString() {
        LearningItem item = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));

        assertThatCode(() -> assertThat(item.toString()).contains("LearningItem{id=" + LEARNING_ITEM_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void learningItemEquals() {
        LearningItem first = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));
        LearningItem second = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new LearningItem());
            assertThat(new LearningItem()).isNotEqualTo(new LearningItem());
        }).doesNotThrowAnyException();
    }

    @Test
    void learningItemHashCode() {
        LearningItem item = transactionTemplate.execute(_ -> entityManager.find(LearningItem.class, LEARNING_ITEM_ID));

        assertThatCode(() -> assertThat(item.hashCode()).isEqualTo("LearningItem".hashCode())).doesNotThrowAnyException();
    }

    @Test
    void learningCardToString() {
        LearningCard card = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));

        assertThatCode(() -> assertThat(card.toString())
                .contains("LearningCard{id=" + CARD_A_ID)
                .contains("(LAZY LOADING)"))
                .doesNotThrowAnyException();
    }

    @Test
    void learningCardEquals() {
        LearningCard first = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));
        LearningCard second = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new LearningCard());
            assertThat(new LearningCard()).isNotEqualTo(new LearningCard());
        }).doesNotThrowAnyException();
    }

    @Test
    void learningCardHashCode() {
        LearningCard card = transactionTemplate.execute(_ -> entityManager.find(LearningCard.class, CARD_A_ID));

        assertThatCode(() -> assertThat(card.hashCode()).isEqualTo("LearningCard".hashCode())).doesNotThrowAnyException();
    }

    @Test
    void exerciseSessionToString() {
        ExerciseSession session = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));

        assertThatCode(() -> assertThat(session.toString())
                .contains("ExerciseSession{id=" + IN_PROGRESS_SESSION_ID)
                .contains("(LAZY LOADING)"))
                .doesNotThrowAnyException();
    }

    @Test
    void exerciseSessionEquals() {
        ExerciseSession first = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));
        ExerciseSession second = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new ExerciseSession());
            assertThat(new ExerciseSession()).isNotEqualTo(new ExerciseSession());
        }).doesNotThrowAnyException();
    }

    @Test
    void exerciseSessionHashCode() {
        ExerciseSession session = transactionTemplate.execute(_ -> entityManager.find(ExerciseSession.class, IN_PROGRESS_SESSION_ID));

        assertThatCode(() -> assertThat(session.hashCode()).isEqualTo("ExerciseSession".hashCode())).doesNotThrowAnyException();
    }

    @Test
    void exerciseAttemptToString() {
        ExerciseAttempt attempt = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));

        assertThatCode(() -> assertThat(attempt.toString())
                .contains("ExerciseAttempt{id=" + FIRST_ATTEMPT_ID)
                .contains("(LAZY LOADING)"))
                .doesNotThrowAnyException();
    }

    @Test
    void exerciseAttemptEquals() {
        ExerciseAttempt first = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));
        ExerciseAttempt second = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));

        assertThatCode(() -> {
            assertThat(first).isEqualTo(second);
            assertThat(first).isNotEqualTo(new ExerciseAttempt());
            assertThat(new ExerciseAttempt()).isNotEqualTo(new ExerciseAttempt());
        }).doesNotThrowAnyException();
    }

    @Test
    void exerciseAttemptHashCode() {
        ExerciseAttempt attempt = transactionTemplate.execute(_ -> entityManager.find(ExerciseAttempt.class, FIRST_ATTEMPT_ID));

        assertThatCode(() -> assertThat(attempt.hashCode()).isEqualTo("ExerciseAttempt".hashCode())).doesNotThrowAnyException();
    }
}
