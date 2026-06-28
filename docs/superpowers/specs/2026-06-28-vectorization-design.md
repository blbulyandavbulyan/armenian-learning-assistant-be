# Vectorization Design Spec

## Goal
Implement vectorization (embeddings) for both individual phrases and full dialogues to enable deduplication, semantic search, and advanced recommendation (spaced repetition) features.

## Architecture
We use Spring AI's `EmbeddingModel` (backed by Gemini multilingual embedding models) to map text to vectors (`vector(3072)`).
Because the AI models are naturally multilingual, we do not need separate embeddings per language. Instead, we generate a single, highly robust "combo-string" embedding for each item.

### 1. Phrase Vectorization
**Trigger:** Whenever a new `Phrase` is created.
**Approach:** Create an "English Anchor" combo-string containing the phrase and its translations.
- Format: `"Armenian: [phrase_text], English: [english_translation], Russian: [russian_translation]"` (dynamically built based on available translations).
- The resulting text is passed to `EmbeddingModel`.
- The returned vector is saved to the `embedding` column on the `phrases` table.
- **Benefit:** High semantic accuracy, searchable across multiple languages, anchors nuanced Armenian terms with English equivalents.

### 2. Dialogue Vectorization
**Trigger:** Whenever a new `Dialogue` is generated and saved.
**Approach:** Chronological Concatenation.
- Format: We iterate over the `dialogue_phrases` in order. For each phrase, we append its combo-string to a single dialogue-level text block.
- Example: 
  `"Speaker 1: Armenian: Բարև, English: Hello. \n Speaker 2: Armenian: Ինչպե՞ս ես, English: How are you?"`
- The concatenated text is passed to `EmbeddingModel`.
- The returned vector is saved to the `embedding` column on the `dialogues` table.
- **Benefit:** Simple and fully captures the conversation's context for full-dialogue deduplication and broad semantic search.

## Data Flow
- **Phrase Creation Flow:** When saving a phrase, build combo-string -> generate embedding -> save.
- **Dialogue Creation Flow:** Collect phrases -> build dialogue-level concatenated string -> generate embedding -> save dialogue with vector.
- Both flows will run asynchronously or outside the critical path of standard API responses if possible, but initially can be synchronous if fast enough.
