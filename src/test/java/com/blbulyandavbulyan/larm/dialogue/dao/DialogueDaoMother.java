package com.blbulyandavbulyan.larm.dialogue.dao;

import java.util.Set;

import com.blbulyandavbulyan.larm.phrase.dao.Dialogue;
import com.blbulyandavbulyan.larm.phrase.dao.DialoguePhrase;
import com.blbulyandavbulyan.larm.phrase.dao.DialogueSpeaker;

public interface DialogueDaoMother {

    interface DefaultDialogue {

        static Dialogue build() {
            return Dialogue.builder()
                    .speakers(Set.of(
                            DialogueSpeaker.builder().speakerRefId("speaker1").build(),
                            DialogueSpeaker.builder().speakerRefId("speaker2").build()
                    ))
                    .dialoguePhrases(Set.of(
                            DialoguePhrase.builder().orderIndex(0).build(),
                            DialoguePhrase.builder().orderIndex(1).build(),
                            DialoguePhrase.builder().orderIndex(2).build()
                    ))
                    .build();
        }
    }
}
