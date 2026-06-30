CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE phrases
(
    id            UUID PRIMARY KEY,
    iso_language_code         CHAR(2) NOT NULL CHECK (iso_language_code ~ '^[a-z]{2}$') DEFAULT 'am',
    phrase        TEXT NOT NULL,
    status VARCHAR(25) NOT NULL,
    transcription TEXT,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_phrase UNIQUE (phrase)
);

CREATE TABLE medias
(
    id               UUID PRIMARY KEY,
    phrase_id        UUID         NOT NULL REFERENCES phrases (id) ON DELETE CASCADE,

-- Future-proofing fields
    storage_provider VARCHAR(50)  NOT NULL, -- 'LOCAL', 'AWS_S3', 'SUPABASE_STORAGE'
    storage_bucket   VARCHAR(255) NOT NULL, -- e.g., 'armenian-app-audio' or 'local-uploads'
    storage_key      VARCHAR(512) NOT NULL, -- e.g., 'phrases/12345/audio_male.mp3'

-- Useful Universal Metadata
    content_type     VARCHAR(100) NOT NULL,          -- 'audio/mpeg', 'audio/wav'
    file_size_bytes  INT NOT NULL,

    ai_model_used    VARCHAR(100),          -- 'Gemini-1.5-Flash', 'ElevenLabs-v2'
    voice_identifier VARCHAR(100),          -- 'male-eastern-1', 'female-western-2'

    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_medias_phrase_id ON medias (phrase_id);