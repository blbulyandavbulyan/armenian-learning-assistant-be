# TODO
This file will contain all the ideas which I consider interesting and worth adding here

## Dialogues
Probably worth trying idea with 'dialogues', when LLM generates the dialogue,
and there is dedicated endpoint for saving dialogue and getting your dialogues.
Which should preserve the order of the phrases.
- [ ] Dedicated endpoint for saving dialogues (preserving order)
- [ ] Dedicated endpoint for retrieving dialogues (preserving order)
- [ ] UI for role-playing dialogues (alternating between speakers)

## Tracking Learning Progress
There should be some convenient way of tracking the user progress, depending on the mode which the user did.
Like if user learnt phrases or whole dialogue.
We should somehow track that progress, and it might be separate kind of progress.
- [ ] Plan the design, suggest the database changes
- [ ] Implement required endpoints which can do the job and document them
- [ ] User "Confidence Levels" (1-4 scale: Again, Hard, Good, Easy) per phrase/dialogue

## Smart Learning Algorithm (SRS)
There should be some kind of smart learning algorithm for repeating phrases and dialogues, which will be really helpful, and won't suggest same phrases over and over again.
- [ ] Implement Spaced Repetition System (SRS) logic
- [ ] Way of learning phrases: should not be repeated, and the app should suggest something which you are likely to forget, but without overwhelming you.
- [ ] There should be a way to mark phrase or dialogue as 'forgotten' so it will be suggested as soon as possible for learning
- [ ] Reverse Learning Mode: Active recall (Translate English/Russian → Armenian)

## Linguistic & Cultural Depth
- [ ] Add 'grammar_note' field to phrases (AI-generated explanation of cases/syntax)
- [ ] Support for Eastern/Western dialect tagging/toggling
- [ ] Lemma/Root word extraction for key vocabulary to map connections between phrases
- [ ] Transliteration toggle (Standard transcription vs. custom phonetic)

## Gamification & Engagement
- [ ] Sentence reconstruction mini-game (unscramble Armenian words)
- [ ] Pronunciation feedback using STT (Speech-to-Text) to compare user voice with TTS
- [ ] Image generation for visual mnemonics per phrase/dialogue (AI-generated visuals)

## Technical Enhancements
- [ ] Semantic search/similarity for "related phrases" discovery using pgvector
- [ ] Export to Anki? (CSV/Apkg)
- [ ] Bulk audio generation for existing phrases without media
- [ ] Multi-voice TTS support (switching between different Piper voices)
 