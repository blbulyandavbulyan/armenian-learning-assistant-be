CREATE TABLE phrases
(
    id            UUID PRIMARY KEY,
    language         CHAR(2) NOT NULL CHECK (language ~ '^[a-z]{2}$') DEFAULT 'am',
    phrase        TEXT NOT NULL,
    transcription TEXT,
    audio_file_id TEXT,
    embedding     vector(1536),
    CONSTRAINT unique_phrase UNIQUE (phrase)
);

CREATE TABLE translations
(
    id               UUID PRIMARY KEY,
    phrase_id        UUID    NOT NULL REFERENCES phrases (id),
    language         CHAR(2) NOT NULL CHECK (language ~ '^[a-z]{2}$'),
    translation_text TEXT    NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audio_files
(
    id               UUID PRIMARY KEY,
    phrase_id        UUID         NOT NULL REFERENCES phrases (id),

-- Future-proofing fields
    storage_provider VARCHAR(50)  NOT NULL, -- 'LOCAL', 'AWS_S3', 'SUPABASE_STORAGE'
    storage_bucket   VARCHAR(255) NOT NULL, -- e.g., 'armenian-app-audio' or 'local-uploads'
    storage_key      VARCHAR(512) NOT NULL, -- e.g., 'phrases/12345/audio_male.mp3'

-- Useful Universal Metadata
    content_type     VARCHAR(100) NOT NULL,          -- 'audio/mpeg', 'audio/wav'
    file_size_bytes  BIGINT NOT NULL,
    duration_seconds DECIMAL(5, 2) NOT NULL,         -- Essential for UI players to show track length

    ai_model_used    VARCHAR(100),          -- 'Gemini-1.5-Flash', 'ElevenLabs-v2'
    voice_identifier VARCHAR(100),          -- 'male-eastern-1', 'female-western-2'

    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);