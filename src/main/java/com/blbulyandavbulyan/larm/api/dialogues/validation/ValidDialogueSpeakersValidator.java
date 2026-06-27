package com.blbulyandavbulyan.larm.api.dialogues.validation;

import java.util.Set;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.api.dialogues.SaveDialogueRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDialogueSpeakersValidator implements ConstraintValidator<ValidDialogueSpeakers, SaveDialogueRequest> {

    @Override
    public boolean isValid(SaveDialogueRequest request, ConstraintValidatorContext context) {
        if (request == null || request.speakers() == null || request.dialoguePhrases() == null) {
            return true; // Let @NotNull and @NotEmpty handle this
        }

        Set<String> definedSpeakers = getDefinedSpeakers(request);
        Set<String> referencedSpeakers = getReferencedSpeakers(request);

        boolean isValid = true;
        
        // Check if all referenced speakers are defined
        for (String referencedSpeaker : referencedSpeakers) {
            if (!definedSpeakers.contains(referencedSpeaker)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Phrase references undefined speaker: " + referencedSpeaker)
                        .addPropertyNode("dialoguePhrases")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        // Check if all defined speakers are used
        for (String definedSpeaker : definedSpeakers) {
            if (!referencedSpeakers.contains(definedSpeaker)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Defined speaker is never used: " + definedSpeaker)
                        .addPropertyNode("speakers")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }

    private static Set<String> getReferencedSpeakers(SaveDialogueRequest request) {
        return request.dialoguePhrases().stream()
                .map(SaveDialogueRequest.DialoguePhraseRequest::speakerId)
                .collect(Collectors.toSet());
    }

    private static Set<String> getDefinedSpeakers(SaveDialogueRequest request) {
        return request.speakers().stream()
                .map(SaveDialogueRequest.SpeakerRequest::id)
                .collect(Collectors.toSet());
    }
}
