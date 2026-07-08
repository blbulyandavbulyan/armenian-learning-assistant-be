INSERT INTO phrases(id, iso_language_code, phrase, transcription)
VALUES ('506eee9d-8833-4052-a139-cf68a77b59e3', 'hy', 'Որտե՞ղ է հացի բաժինը:', 'Vortegh e hatsi bazhiny?');


INSERT INTO medias(id, phrase_id, storage_provider, storage_bucket, storage_key, content_type, file_size_bytes, ai_model_used,
                   voice_identifier)

VALUES ('b352560f-58f9-4c3e-8f37-46be09978511', '506eee9d-8833-4052-a139-cf68a77b59e3', 'LOCAL', 'somebucket',
        'some-existing-file.wav', 'audio/wav', 19, 'piper', 'default-voice');
