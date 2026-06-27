package com.blbulyandavbulyan.larm.dialogue;

public interface DialogueSavingService {
    /**
     * Saves a dialogue: stores phrases, speakers, and metadata directly to the database.
     *
     * @param parameters the dialogue to save
     * @return the saved dialogue resource with its generated ID
     */
    SavedDialogueResource saveDialogue(StoreDialogueParameters parameters);
}
