CREATE TABLE learning_items
(
    id                UUID PRIMARY KEY,
    user_id           UUID                     NOT NULL REFERENCES users (id),
    -- soft reference: points to dialogues.id today, to other content tables later
    content_item_id   UUID                     NOT NULL,
    content_item_type VARCHAR(32)              NOT NULL,
    enrolled_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    excluded_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_learning_items_content_item_type CHECK (content_item_type IN ('DIALOGUE')),
    CONSTRAINT uq_learning_items_user_content UNIQUE (user_id, content_item_id, content_item_type)
);

CREATE TABLE learning_cards
(
    id               UUID PRIMARY KEY,
    learning_item_id UUID                     NOT NULL REFERENCES learning_items (id),
    exercise_type    VARCHAR(32)              NOT NULL,
    -- DIALOGUE: dialogue_speakers.id (the role the user plays); NULL for content without variants
    variant_id       UUID,
    next_review_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    interval_days    INTEGER                  NOT NULL DEFAULT 0 CHECK (interval_days >= 0),
    ease_factor      DOUBLE PRECISION         NOT NULL DEFAULT 2.5 CHECK (ease_factor >= 1.3),
    repetition_count INTEGER                  NOT NULL DEFAULT 0 CHECK (repetition_count >= 0),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_learning_cards_exercise_type CHECK (exercise_type IN ('TYPING')),
    CONSTRAINT uq_learning_cards_item_exercise_variant UNIQUE NULLS NOT DISTINCT (learning_item_id, exercise_type, variant_id)
);

CREATE TABLE exercise_sessions
(
    id               UUID PRIMARY KEY,
    learning_card_id UUID                     NOT NULL REFERENCES learning_cards (id),
    user_id          UUID                     NOT NULL REFERENCES users (id),
    aggregate_score  INTEGER CHECK (aggregate_score BETWEEN 0 AND 100),
    started_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at     TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_exercise_sessions_completed_has_score CHECK ((completed_at IS NULL) = (aggregate_score IS NULL))
);

-- at most one open session per card
CREATE UNIQUE INDEX uq_exercise_sessions_one_in_progress
    ON exercise_sessions (learning_card_id)
    WHERE completed_at IS NULL;

CREATE TABLE exercise_attempts
(
    id                  UUID PRIMARY KEY,
    session_id          UUID                     NOT NULL REFERENCES exercise_sessions (id),
    content_sub_item_id UUID                     NOT NULL,
    answer_type         VARCHAR(32)              NOT NULL,
    answer_text         TEXT,
    used_hint           BOOLEAN                  NOT NULL,
    raw_quality         INTEGER                  NOT NULL CHECK (raw_quality BETWEEN 0 AND 5),
    quality             INTEGER                  NOT NULL CHECK (quality BETWEEN 0 AND 5),
    correct             BOOLEAN                  NOT NULL,
    evaluated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_exercise_attempts_answer_type CHECK (answer_type IN ('TEXT')),
    CONSTRAINT chk_exercise_attempts_text_answer CHECK (answer_type <> 'TEXT' OR answer_text IS NOT NULL)
);

CREATE INDEX idx_learning_cards_item_review ON learning_cards (learning_item_id, next_review_at);
CREATE INDEX idx_exercise_sessions_user_completed ON exercise_sessions (user_id, completed_at);
CREATE INDEX idx_exercise_attempts_session ON exercise_attempts (session_id);
