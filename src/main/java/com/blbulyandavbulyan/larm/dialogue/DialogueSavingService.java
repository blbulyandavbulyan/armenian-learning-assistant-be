package com.blbulyandavbulyan.larm.dialogue;

public interface DialogueSavingService {
    /**
     * Saves a dialogue: stores speakers and metadata directly to the database.
     * Assumes phrases have already been processed and persisted.
     *
     * @param parameters the dialogue to save
     * @return the saved dialogue resource with its generated ID
     */
    SavedDialogueResource saveDialogue(StoreDialogueParameters parameters);
}
