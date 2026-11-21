package com.blbulyandavbulyan.larm.exercise;

import java.util.List;
import java.util.UUID;

public record SubItemAttemptsResource(UUID contentSubItemId, List<AttemptResource> attempts) {
}
