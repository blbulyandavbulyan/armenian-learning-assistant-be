# Dialogue Phrase Deduplication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deduplicate standalone phrases when saving dialogues, and enforce contextual translation consistency by using dedicated JPA Entities (instead of global dictionaries or meaningless Maps).

**Architecture:** We have dropped the global `Translation` entity and `translations` table. We will replace them with three dedicated JPA Entities (`DialogueTitleTranslation`, `DialogueSpeakerTranslation`, `DialoguePhraseTranslation`) implementing a common `ContextualTranslation` interface. They will map to three tables.

**Tech Stack:** Java 25, Spring Boot, Spring Data JPA, Flyway, PostgreSQL.

## Global Constraints

- Must follow Spring Modulith principles (keep boundaries clean).
- Always use `mvn verify` instead of `mvn test`.
- Mock external dependencies (`ChatClient`, etc.) only at the boundary in IT tests.
- Flyway migrations must be used for schema changes.

---

### Task 1: Update Entities and Create Flyway Migration (COMPLETED)
*(Completed)* Removed global translations table and created initial contextual tables without IDs.

### Task 2: Implement Batch Phrase Deduplication Query (COMPLETED)
*(Completed)* Re-implemented phrase deduplication logic.

---

### Task 3: Add Proper Entity Models for Contextual Translations

**Files:**
- Create: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/ContextualTranslation.java` (Interface)
- Create: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/DialogueTitleTranslation.java`
- Create: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/DialogueSpeakerTranslation.java`
- Create: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/DialoguePhraseTranslation.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/Dialogue.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/DialogueSpeaker.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dao/entities/DialoguePhrase.java`
- Modify: `src/main/resources/db/migration/V2__create_dialogue_tables.sql`

**Interfaces:**
- Produces: Proper Domain-Driven Design JPA entities for translations.

- [ ] **Step 1: Modify original Flyway script**

Modify `src/main/resources/db/migration/V2__create_dialogue_tables.sql` to add `id UUID PRIMARY KEY` to the 3 new translation tables, and a `UNIQUE` constraint instead of composite primary keys.

For example, replace the translation tables with:
```sql
CREATE TABLE dialogue_title_translations (
    id UUID PRIMARY KEY,
    dialogue_id UUID NOT NULL REFERENCES dialogues(id) ON DELETE CASCADE,
    iso_language_code CHAR(2) NOT NULL CHECK (iso_language_code ~ '^[a-z]{2}$'),
    translation_text TEXT NOT NULL
);

CREATE TABLE dialogue_speaker_translations (
    id UUID PRIMARY KEY,
    dialogue_speaker_id UUID NOT NULL REFERENCES dialogue_speakers(id) ON DELETE CASCADE,
    iso_language_code CHAR(2) NOT NULL CHECK (iso_language_code ~ '^[a-z]{2}$'),
    translation_text TEXT NOT NULL
);

CREATE TABLE dialogue_phrase_translations (
    id UUID PRIMARY KEY,
    dialogue_phrase_id UUID NOT NULL REFERENCES dialogue_phrases(id) ON DELETE CASCADE,
    iso_language_code CHAR(2) NOT NULL CHECK (iso_language_code ~ '^[a-z]{2}$'),
    translation_text TEXT NOT NULL
);
```

- [ ] **Step 2: Create ContextualTranslation Interface**

```java
package com.blbulyandavbulyan.larm.dao.entities;

import java.util.UUID;

public interface ContextualTranslation {
    UUID getId();
    String getIsoLanguageCode();
    String getTranslationText();
}
```

- [ ] **Step 3: Create Translation Entities**

Create `DialogueTitleTranslation`, `DialogueSpeakerTranslation`, and `DialoguePhraseTranslation` as standard `@Entity` classes mapping to the 3 tables, each implementing `ContextualTranslation`. They should have an `id`, `isoLanguageCode`, `translationText`. You do not need to add the `@ManyToOne` parent reference inside them if you use a `@JoinColumn` on the parent side, OR you can add it if preferred.

- [ ] **Step 4: Update Dialogue Entities**

Replace the `@ElementCollection` Maps in `Dialogue`, `DialogueSpeaker`, and `DialoguePhrase` with `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` sets of the new entities. Use `@JoinColumn` if you didn't define the `@ManyToOne` back-reference.

- [ ] **Step 5: Run build**

Run: `mvn clean compile`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/blbulyandavbulyan/larm/dao/entities/ src/main/resources/db/migration/
git commit -m "feat: Convert contextual translations to proper JPA entities"
```

---

### Task 4: Update Dialogue Saving Logic & Mappers

**Files:**
- Modify: `src/main/java/com/blbulyandavbulyan/larm/dialogue/service/DefaultDialogueSavingService.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueResponseMapper.java`
- Modify: `src/main/java/com/blbulyandavbulyan/larm/api/phrases/PhraseResponseMapper.java`
- Modify: `src/test/resources/sql/insert-dialogue.sql` (Update to include UUIDs for translations)

**Interfaces:**
- Updates dialogue creation to populate JPA entity sets instead of Maps.
- Updates API mappers to inject contextual translations into `PhraseResponse`.

- [ ] **Step 1: Populate Entity Sets**

In `DefaultDialogueSavingService.java`, when assigning translations to `Dialogue`, `DialogueSpeaker`, and `DialoguePhrase`, instantiate the new `Dialogue...Translation` entities and add them to the parent's Set. Give them a random UUID (`UUID.randomUUID()`).

- [ ] **Step 2: Update SQL test scripts**

Update `insert-dialogue.sql` (and any others inserting translations) to insert a random UUID in the new translation tables since they now require an `id` column.

- [ ] **Step 3: Update API Mappers**

Update `PhraseResponseMapper.java` to optionally accept a collection of contextual translations to inject into `PhraseResponse`.
Update `DialogueResponseMapper.java` to pass `dialogue.getTitleTranslations()`, `speaker.getTranslations()`, etc. when mapping `Phrase` to `PhraseResponse`, so the API response structure (`get-dialogue-response.json`) remains completely unchanged.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/blbulyandavbulyan/larm/dialogue/service/ src/test/resources/sql/
git commit -m "feat: Link contextual translations to dialogue entities"
```

---

### Task 5: Fix and Run Integration Tests

**Files:**
- Modify: `src/test/java/com/blbulyandavbulyan/larm/...` (Tests affected by this)

**Interfaces:**
- N/A

- [ ] **Step 1: Fix broken tests**

Identify any test failures from the structural changes. 
Update `DialogueControllerIT` or test records as needed.

- [ ] **Step 2: Run all tests**

Run: `mvn clean verify`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/
git commit -m "test: Update tests for phrase deduplication and entity translations"
```
