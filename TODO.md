# TODO
This file will contain all the ideas which I consider interesting and worth adding here
## Dialogues

Probably worth trying idea with 'dialogues', when LLM generates the dialogue,
and there is dedicated endpoint for saving dialogue and getting your dialogues.
Which should preserve the order of the phrases.
- [ ] Dedicated endpoint for saving dialogues (preserving order)
- [ ] dedicated endpoint for retrieving dialogues (preserving order)

## Tracking learning progress
There should be some convenient way of tracking the user progress, depending on the mode which the user did.
Like if user learnt phrases or whole dialogue.
We should somehow track that progress, and it might be separate kind of progress.
- [ ] Plan the design, suggest the database changes
- [ ] Implement required endpoints which can do the job and document them

## Some kind of smart learning algorithm
There should be some kind of smart learning algorithm for repeating phrases and dialogues, which will be really helpful, and won't suggest same phrases over and over again.
- [ ] Way of learning phrases, should not be repeated, and the app should suggest something which you are likely to forget, but without overwhelming you.
- [ ] There should be a way to mark phrase or dialogue as 'forgotten' so it will be suggested as soon as possible for learning
- [ ] 