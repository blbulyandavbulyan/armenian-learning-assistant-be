INSERT INTO phrases (id, iso_language_code, phrase, status, transcription, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'am', 'In the bakery', 'APPROVED', 'In the bakery trans', CURRENT_TIMESTAMP),
('22222222-2222-2222-2222-222222222222', 'am', 'Baker', 'APPROVED', 'Baker trans', CURRENT_TIMESTAMP),
('33333333-3333-3333-3333-333333333333', 'am', 'Customer', 'APPROVED', 'Customer trans', CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444444', 'am', 'Բարեւ ձեզ', 'APPROVED', 'Barev dzez', CURRENT_TIMESTAMP),
('55555555-5555-5555-5555-555555555555', 'am', 'Բարեւ ձեզ, խնդրում եմ մեկ հաց:', 'APPROVED', 'Barev dzez, khndrum em mek hats', CURRENT_TIMESTAMP),
('66666666-6666-6666-6666-666666666666', 'am', 'Ահա, խնդրեմ:', 'APPROVED', 'Aha, khndrem', CURRENT_TIMESTAMP);

INSERT INTO translations (id, phrase_id, iso_language_code, translation_text, created_at) VALUES
('77777777-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'en', 'In the bakery', CURRENT_TIMESTAMP),
('77777777-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'en', 'Baker', CURRENT_TIMESTAMP),
('77777777-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 'en', 'Customer', CURRENT_TIMESTAMP),
('77777777-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 'en', 'Hello', CURRENT_TIMESTAMP),
('77777777-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 'en', 'Hello, one bread please', CURRENT_TIMESTAMP),
('77777777-6666-6666-6666-666666666666', '66666666-6666-6666-6666-666666666666', 'en', 'Here you go', CURRENT_TIMESTAMP);

INSERT INTO medias (id, phrase_id, storage_provider, storage_bucket, storage_key, content_type, file_size_bytes, ai_model_used, voice_identifier, created_at) VALUES
('88888888-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'LOCAL', 'bucket', 'key1', 'audio/wav', 1234, 'model', 'voice', CURRENT_TIMESTAMP),
('88888888-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'LOCAL', 'bucket', 'key2', 'audio/wav', 1234, 'model', 'voice', CURRENT_TIMESTAMP),
('88888888-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 'LOCAL', 'bucket', 'key3', 'audio/wav', 1234, 'model', 'voice', CURRENT_TIMESTAMP),
('88888888-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 'LOCAL', 'bucket', 'key4', 'audio/wav', 1234, 'model', 'voice', CURRENT_TIMESTAMP),
('88888888-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 'LOCAL', 'bucket', 'key5', 'audio/wav', 1234, 'model', 'voice', CURRENT_TIMESTAMP),
('88888888-6666-6666-6666-666666666666', '66666666-6666-6666-6666-666666666666', 'LOCAL', 'bucket', 'key6', 'audio/wav', 1234, 'model', 'voice', CURRENT_TIMESTAMP);

INSERT INTO dialogues (id, title_phrase_id, created_at) VALUES
('99999999-9999-9999-9999-999999999999', '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP);

INSERT INTO dialogue_speakers (id, dialogue_id, speaker_ref_id, name_phrase_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '99999999-9999-9999-9999-999999999999', 'speaker1', '22222222-2222-2222-2222-222222222222'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '99999999-9999-9999-9999-999999999999', 'speaker2', '33333333-3333-3333-3333-333333333333');

INSERT INTO dialogue_phrases (id, dialogue_id, phrase_id, speaker_id, order_index) VALUES
('cccccccc-cccc-cccc-cccc-cccccccccccc', '99999999-9999-9999-9999-999999999999', '44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0),
('dddddddd-dddd-dddd-dddd-dddddddddddd', '99999999-9999-9999-9999-999999999999', '55555555-5555-5555-5555-555555555555', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 1),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '99999999-9999-9999-9999-999999999999', '66666666-6666-6666-6666-666666666666', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 2);
