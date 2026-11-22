package com.blbulyandavbulyan.larm.exercise;

import java.util.UUID;

public record AttemptCommand(UUID contentSubItemId, boolean usedHint, Answer answer) {
}
