package com.blbulyandavbulyan.larm.dialogue;

public interface DialogueSavingService {
    /**
     * Saves a dialogue: processes each phrase through TTS, stores audio, saves
     * speakers, dialogue metadata (with embedding), and ordered dialogue phrases.
     *
     * @param parameters the dialogue to save
     * @return the saved dialogue resource with its generated ID
     */
    SavedDialogueResource saveDialogue(SaveDialogueParameters parameters);
}
