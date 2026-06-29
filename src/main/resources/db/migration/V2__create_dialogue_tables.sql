CREATE TABLE dialogues
(
    id              UUID PRIMARY KEY,
    title_phrase_id UUID NOT NULL REFERENCES phrases (id),
    embedding       vector(3072) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE dialogue_speakers
(
    id               UUID PRIMARY KEY,
    dialogue_id      UUID NOT NULL REFERENCES dialogues (id),
    name_phrase_id   UUID NOT NULL REFERENCES phrases (id),
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE dialogue_phrases
(
    id          UUID    PRIMARY KEY,
    dialogue_id UUID    NOT NULL REFERENCES dialogues (id),
    phrase_id   UUID    NOT NULL REFERENCES phrases (id),
    speaker_id  UUID    NOT NULL REFERENCES dialogue_speakers (id) DEFERRABLE INITIALLY DEFERRED,
    order_index INTEGER NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_dialogue_phrase_order UNIQUE (dialogue_id, order_index)
);

CREATE INDEX idx_dialogue_speakers_name_phrase_id ON dialogue_speakers (name_phrase_id);
CREATE INDEX idx_dialogues_title_phrase_id ON dialogues (title_phrase_id);
CREATE INDEX idx_dialogue_phrases_phrase_id ON dialogue_phrases (phrase_id);
CREATE INDEX idx_dialogue_phrases_speaker_id ON dialogue_phrases (speaker_id);

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
