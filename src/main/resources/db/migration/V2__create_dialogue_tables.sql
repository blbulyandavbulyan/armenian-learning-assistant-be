CREATE TABLE dialogue_speakers
(
    id               UUID PRIMARY KEY,
    speaker_ref_id   VARCHAR(100) NOT NULL,
    title            TEXT         NOT NULL,
    transcription    TEXT         NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE dialogue_speaker_translations
(
    id                UUID PRIMARY KEY,
    speaker_id        UUID NOT NULL REFERENCES dialogue_speakers (id),
    iso_language_code CHAR(2) NOT NULL CHECK (iso_language_code ~ '^[a-z]{2}$'),
    translation_text  TEXT NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE dialogues
(
    id               UUID PRIMARY KEY,
    title            TEXT         NOT NULL,
    transcription    TEXT         NOT NULL,
    embedding        vector(1536),
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE dialogue_title_translations
(
    id                UUID PRIMARY KEY,
    dialogue_id       UUID NOT NULL REFERENCES dialogues (id),
    iso_language_code CHAR(2) NOT NULL CHECK (iso_language_code ~ '^[a-z]{2}$'),
    translation_text  TEXT NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE dialogue_phrases
(
    id          UUID    PRIMARY KEY,
    dialogue_id UUID    NOT NULL REFERENCES dialogues (id),
    phrase_id   UUID    NOT NULL REFERENCES phrases (id),
    speaker_id  UUID    NOT NULL REFERENCES dialogue_speakers (id),
    order_index INTEGER NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT unique_dialogue_phrase_order UNIQUE (dialogue_id, order_index)
);

CREATE INDEX idx_dialogue_phrases_phrase_id ON dialogue_phrases (phrase_id);
CREATE INDEX idx_dialogue_phrases_speaker_id ON dialogue_phrases (speaker_id);
CREATE INDEX idx_dialogue_title_translations_dialogue_id ON dialogue_title_translations (dialogue_id);
CREATE INDEX idx_dialogue_speaker_translations_speaker_id ON dialogue_speaker_translations (speaker_id);
