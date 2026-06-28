-- Phrase 1
INSERT INTO phrases(id, iso_language_code, phrase, status, transcription, embedding)
VALUES ('506eee9d-8833-4052-a139-cf68a77b59e3', 'hy', 'Որտե՞ղ է հացի բաժինը:', 'DRAFT', 'Vortegh e hatsi bazhiny?', array_fill(0, ARRAY[3072])::vector);

INSERT INTO translations(id, phrase_id, iso_language_code, translation_text)
VALUES ('bc5333d4-c70b-4121-8bdc-4b94547de419', '506eee9d-8833-4052-a139-cf68a77b59e3', 'en', 'Where is the bread section?'),
       ('b7dc62e1-c7eb-4c29-bf5a-7ccc26c73dd2', '506eee9d-8833-4052-a139-cf68a77b59e3', 'ru', 'Где находится отдел хлеба?');

INSERT INTO medias(id, phrase_id, storage_provider, storage_bucket, storage_key, content_type, file_size_bytes, ai_model_used,
                   voice_identifier)
VALUES ('b352560f-58f9-4c3e-8f37-46be09978511', '506eee9d-8833-4052-a139-cf68a77b59e3', 'LOCAL', 'somebucket',
        'some-existing-file-1.wav', 'audio/wav', 19, 'piper', 'default-voice');

-- Phrase 2
INSERT INTO phrases(id, iso_language_code, phrase, status, transcription, embedding)
VALUES ('7a2d8b5c-4f1e-4c7a-9d3b-8f2e1a9c3d4e', 'hy', 'Բարև ձեզ', 'DRAFT', 'Barev dzez', array_fill(0, ARRAY[3072])::vector);

INSERT INTO translations(id, phrase_id, iso_language_code, translation_text)
VALUES ('a1b2c3d4-e5f6-4a7b-8c9d-e0f1a2b3c4d5', '7a2d8b5c-4f1e-4c7a-9d3b-8f2e1a9c3d4e', 'en', 'Hello');

-- Phrase 3
INSERT INTO phrases(id, iso_language_code, phrase, status, transcription, embedding)
VALUES ('d4e5f6a1-b2c3-4d5e-9f7a-8b9c0d1e2f3a', 'hy', 'Ինչպե՞ս եք:', 'DRAFT', 'Inchpes eq?', array_fill(0, ARRAY[3072])::vector);

INSERT INTO translations(id, phrase_id, iso_language_code, translation_text)
VALUES ('f1e2d3c4-b5a6-4c7d-8e9f-0a1b2c3d4e5f', 'd4e5f6a1-b2c3-4d5e-9f7a-8b9c0d1e2f3a', 'en', 'How are you?');
