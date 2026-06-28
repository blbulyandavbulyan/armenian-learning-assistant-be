INSERT INTO phrases(id, iso_language_code, phrase, status, transcription, embedding)
VALUES ('506eee9d-8833-4052-a139-cf68a77b59e3', 'hy', 'Որտե՞ղ է հացի բաժինը:', 'DRAFT', 'Vortegh e hatsi bazhiny?', array_fill(0, ARRAY[1536])::vector);

INSERT INTO translations(id, phrase_id, iso_language_code, translation_text)
VALUES ('bc5333d4-c70b-4121-8bdc-4b94547de419', '506eee9d-8833-4052-a139-cf68a77b59e3', 'en', 'Where is the bread section?'),
       ('b7dc62e1-c7eb-4c29-bf5a-7ccc26c73dd2', '506eee9d-8833-4052-a139-cf68a77b59e3', 'ru', 'Где находится отдел хлеба?');

INSERT INTO medias(id, phrase_id, storage_provider, storage_bucket, storage_key, content_type, file_size_bytes, ai_model_used,
                   voice_identifier)

VALUES ('b352560f-58f9-4c3e-8f37-46be09978511', '506eee9d-8833-4052-a139-cf68a77b59e3', 'LOCAL', 'somebucket',
        'some-existing-file.wav', 'audio/wav', 19, 'piper', 'default-voice');
