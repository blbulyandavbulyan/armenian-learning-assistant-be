ALTER TABLE dialogue_phrases DROP CONSTRAINT dialogue_phrases_speaker_id_fkey;
ALTER TABLE dialogue_phrases ADD CONSTRAINT dialogue_phrases_speaker_id_fkey FOREIGN KEY (speaker_id) REFERENCES dialogue_speakers(id) DEFERRABLE INITIALLY DEFERRED;
