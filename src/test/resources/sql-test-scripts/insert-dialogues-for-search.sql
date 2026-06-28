-- Insert phrases that will act as titles
INSERT INTO phrases (id, phrase, iso_language_code, status, transcription, embedding, created_at)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'Շուկայում', 'hy', 'APPROVED', 'Shukayum', translate((ARRAY[1.0] || array_fill(0, ARRAY[3071]))::text, '{}', '[]')::vector, NOW()),
    ('22222222-2222-2222-2222-222222222222', 'Հիվանդանոցում', 'hy', 'APPROVED', 'Hivandanotsum', translate((ARRAY[0.1] || array_fill(0, ARRAY[3071]))::text, '{}', '[]')::vector, NOW());

-- Insert translations for titles
INSERT INTO translations (id, phrase_id, iso_language_code, translation_text)
VALUES 
    ('11111111-1111-1111-1111-11111111111a', '11111111-1111-1111-1111-111111111111', 'en', 'At the market'),
    ('22222222-2222-2222-2222-22222222222a', '22222222-2222-2222-2222-222222222222', 'en', 'At the hospital');

-- Insert medias for titles
INSERT INTO medias (id, phrase_id, storage_provider, storage_bucket, storage_key, content_type, file_size_bytes, ai_model_used, voice_identifier)
VALUES 
    ('11111111-1111-1111-1111-11111111111b', '11111111-1111-1111-1111-111111111111', 'LOCAL', 'test-bucket', 'market.wav', 'audio/wav', 100, 'piper', 'default'),
    ('22222222-2222-2222-2222-22222222222b', '22222222-2222-2222-2222-222222222222', 'LOCAL', 'test-bucket', 'hospital.wav', 'audio/wav', 100, 'piper', 'default');

-- Insert dialogues
INSERT INTO dialogues (id, title_phrase_id, embedding, created_at)
VALUES 
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', translate((ARRAY[0.9] || array_fill(0, ARRAY[3071]))::text, '{}', '[]')::vector, NOW()),
    ('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', translate((ARRAY[0.1] || array_fill(0, ARRAY[3071]))::text, '{}', '[]')::vector, NOW());

-- Insert speakers (Phrase for name)
INSERT INTO phrases (id, phrase, iso_language_code, status, transcription, embedding, created_at)
VALUES 
    ('55555555-5555-5555-5555-555555555555', 'Աննա', 'hy', 'APPROVED', 'Anna', translate((ARRAY[0.0] || array_fill(0, ARRAY[3071]))::text, '{}', '[]')::vector, NOW());

INSERT INTO dialogue_speakers (id, dialogue_id, name_phrase_id, created_at)
VALUES 
    ('66666666-6666-6666-6666-666666666666', '33333333-3333-3333-3333-333333333333', '55555555-5555-5555-5555-555555555555', NOW()),
    ('77777777-7777-7777-7777-777777777777', '44444444-4444-4444-4444-444444444444', '55555555-5555-5555-5555-555555555555', NOW());

-- Insert phrases for dialogue
INSERT INTO phrases (id, phrase, iso_language_code, status, transcription, embedding, created_at)
VALUES 
    ('88888888-8888-8888-8888-888888888888', 'Բարեւ', 'hy', 'APPROVED', 'Barev', translate((ARRAY[0.0] || array_fill(0, ARRAY[3071]))::text, '{}', '[]')::vector, NOW());

-- Insert dialogue phrases
INSERT INTO dialogue_phrases (id, dialogue_id, speaker_id, phrase_id, order_index, created_at)
VALUES 
    ('99999999-9999-9999-9999-999999999999', '33333333-3333-3333-3333-333333333333', '66666666-6666-6666-6666-666666666666', '88888888-8888-8888-8888-888888888888', 1, NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', '77777777-7777-7777-7777-777777777777', '88888888-8888-8888-8888-888888888888', 1, NOW());
