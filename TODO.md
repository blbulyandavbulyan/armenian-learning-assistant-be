# TODO
This file will contain all the ideas which I consider interesting and worth adding here

## High Priority (Core Learning Experience):
- [ ] identify if generating prhases and saving phrases separately outside of dialogue is even useful feature. Because dialogues have more context and most probably more fun then separate phrases. Provide arguments for preserving generating and saving phrases separately, and also provide the arguments for dropping this feature.
### Vectorization (Embeddings)
*See design spec: [docs/superpowers/specs/2026-06-28-vectorization-design.md](docs/superpowers/specs/2026-06-28-vectorization-design.md)*
*See implementation plan: [docs/superpowers/plans/2026-06-28-vectorization-plan.md](docs/superpowers/plans/2026-06-28-vectorization-plan.md)*
- [x] Vectorization of phrases in the dialogoue
- [x] Vectorization of full dialogue (maybe by concatenating phrases)

### Tools
#### For generating dialogues
- [ ] Tool for generating dialogue endpoint (POST /chat/dialogue) which uses vector search in postgres to identify if similar dialogues are alerady saved.
- [ ] Adjustemnt to the chat endpoint so that it would return similar dialgoues, probably as list of dialogue ids, so that user can maybe fetch them later. Or maybe it is better to return them in the response fully, I don't know.
#### For generating phrases
- [ ] Tool for generating phrases (POST /chat/phrases) which uses vector search in postgres to identify if similar phrases was already saved.
- [ ] Adjustment in the chat endpoint, so that similar phrases will be returned in the response, as ids or as full phrases.

### Dialogues
Probably worth trying idea with 'dialogues', when LLM generates the dialogue,
and there is dedicated endpoint for saving dialogue and getting your dialogues.
Which should preserve the order of the phrases.
- [x] Dedicated endpoint for saving dialogues (preserving order)
- [x] Dedicated endpoint for retrieving dialogues (preserving order)
- [ ] UI for role-playing dialogues (alternating between speakers)

### Tracking Learning Progress
There should be some convenient way of tracking the user progress, depending on the mode which the user did.
Like if user learnt phrases or whole dialogue.
We should somehow track that progress, and it might be separate kind of progress.
- [ ] Plan the design, suggest the database changes
- [ ] Implement required endpoints which can do the job and document them
- [ ] User "Confidence Levels" (1-4 scale: Again, Hard, Good, Easy) per phrase/dialogue

### Smart Learning Algorithm (SRS)
There should be some kind of smart learning algorithm for repeating phrases and dialogues, which will be really helpful, and won't suggest same phrases over and over again.
- [ ] Implement Spaced Repetition System (SRS) logic
- [ ] Way of learning phrases: should not be repeated, and the app should suggest something which you are likely to forget, but without overwhelming you.
- [ ] There should be a way to mark phrase or dialogue as 'forgotten' so it will be suggested as soon as possible for learning
- [ ] Reverse Learning Mode: Active recall (Translate English/Russian → Armenian)

## Medium Priority (Enhancements leveraging existing tech & educational value):

### Technical Enhancements
- [ ] Investigate and fix duplicate TTS generation and storage for identical phrases.
  - **Problem Statement**: Currently, `PhraseProcessor` generates TTS audio and uploads it to object storage *before* the database layer checks if the phrase already exists. 
  - **Why this is a problem**: If a single dialogue request (or subsequent requests) contains identical phrase texts (e.g. "Բարեւ"), the application calls the Text-to-Speech service multiple times and stores duplicate audio files on disk with different keys. This wastes processing time and disk space.
  - **Why it's tricky to fix**: We can't simply use Spring's `@Cacheable` on the `PhraseProcessor.process()` method. The `SavePhraseParameters` returned by the processor carries the contextual `translations` tied to that specific phrase occurrence. If we cache it using the phrase text as the key, a subsequent identical phrase (with potentially different translations) will receive a cache hit and falsely inherit the translations of the first phrase occurrence. The deduplication logic must decouple the shared audio generation from the context-specific translations. *Keep in mind that translations are currently unused in the PhraseProcessor, at the moment when this todo was written*
- [ ] Semantic search/similarity for "related phrases" discovery using pgvector

### Linguistic & Cultural Depth
- [ ] Add 'grammar_note' field to phrases (AI-generated explanation of cases/syntax)

## Lower Priority (Further Engagement & Polish):

### Gamification & Engagement
- [ ] Pronunciation feedback using STT (Speech-to-Text) to compare user voice with TTS

### Linguistic & Cultural Depth
- [ ] Support for Eastern/Western dialect tagging/toggling
- [ ] Lemma/Root word extraction for key vocabulary to map connections between phrases
- [ ] Transliteration toggle (Standard transcription vs. custom phonetic)

### Technical Enhancements
- [ ] Export to Anki? (CSV/Apkg)
- [ ] Bulk audio generation for existing phrases without media
- [ ] Multi-voice TTS support (switching between different Piper voices)
- [ ] *See, improve, and implement design spec: [docs/design/multiple-voices-for-dialogues.md](docs/design/multiple-voices-for-dialogues.md)*

### Gamification & Engagement
- [ ] Sentence reconstruction mini-game (unscramble Armenian words)
- [ ] Image generation for visual mnemonics per phrase/dialogue (AI-generated visuals)

### Security authentication, authorization
This is just in case, in case if this idea becomes huge and stonks, in this case I have to have this too in the application.
It is the lowest priority, cause there are zero core features which are useful at this moment, meaning that focusing on it right now would just burn the initiative.
But it probably has to be added at some point.
Probably it might be also related to the 'tracking user progress', cause in this case we have to know 'who the user is'