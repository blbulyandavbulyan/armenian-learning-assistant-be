-- Remove vector embedding from phrases to save storage and API cost, as semantic search will be dialogue-level
ALTER TABLE phrases DROP COLUMN embedding;
