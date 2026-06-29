# Dialogue Phrase Deduplication & Contextual Translations Design

## Overview
Currently, saving a dialogue throws a `PhrasesAlreadyExistException` if any generated phrases already exist in the database. 
This design redesigns the phrase saving logic to deduplicate underlying phrases while perfectly retaining their exact contextual translations, completely avoiding any "Frankenstein" inconsistent state.

## Schema Architecture Changes

1. **Drop the Global `translations` Table**
   - The standalone `translations` table and its `Translation` JPA entity will be deleted.
   - We remove `translations` from the `Phrase` entity. A `Phrase` will now strictly represent the raw Armenian text, transcription, and audio media.

2. **Contextual Translation Tables & Entities**
   - Translations will be stored directly with the context that owns them via dedicated JPA entities mapping to three context-specific tables:
     - `dialogue_phrase_translations` (`id`, `dialogue_phrase_id`, `iso_language_code`, `translation_text`)
     - `dialogue_speaker_translations` (`id`, `dialogue_speaker_id`, `iso_language_code`, `translation_text`)
     - `dialogue_title_translations` (`id`, `dialogue_id`, `iso_language_code`, `translation_text`)
   - The tables will not have a unique constraint on language code, allowing multiple translations for the same language (e.g., two English translations for the same phrase).
   - We will create three new JPA `@Entity` classes (`DialogueTitleTranslation`, `DialogueSpeakerTranslation`, `DialoguePhraseTranslation`) that implement a common interface `ContextualTranslation`.
   - The `Dialogue`, `DialogueSpeaker`, and `DialoguePhrase` entities will manage these via `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)`.

## Data Flow (The Pure Deduplication Strategy)

When a dialogue is saved, the orchestration service will process phrases as follows:

1. **Find or Create Phrase Entity:** 
   Look up the standalone phrase by its text. 
   - If it exists, retrieve it (deduplication).
   - If not, create and persist it along with its audio media.
2. **Assemble Contextual Entities:**
   When constructing the `Dialogue`, `DialogueSpeaker`, and `DialoguePhrase` entities:
   - Set their `phrase` foreign key to the found/created `Phrase` from Step 1.
   - Instantiate their respective translation entities (e.g., `DialoguePhraseTranslation`) from the provided `CreateTranslationParameters`, and add them to their `@OneToMany` translation set.
3. **Persist:**
   - Saving the `Dialogue` cascades to save all dialogue phrases and speakers, which in turn seamlessly insert their contextual translations into the new contextual tables.
   - No complex deduplication or matching logic is needed for translations.

## Success Criteria / Trade-offs
- **Guaranteed Consistency:** It is physically impossible for a translation to be associated with the wrong phrase, because it is bound strictly to the dialogue's context.
- **Domain-Driven Entities:** Using proper JPA entities preserves type safety and a clear domain model, unlike raw Maps or unstructured JSON.
- **Simpler Codebase:** Removes the need for complex deduplication matching on translations.
- **Deduplication:** The underlying Armenian `Phrase` string is never duplicated in the DB.
