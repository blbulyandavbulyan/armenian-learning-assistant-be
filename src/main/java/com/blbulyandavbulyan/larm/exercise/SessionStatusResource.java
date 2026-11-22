package com.blbulyandavbulyan.larm.exercise;

import java.util.List;
import java.util.UUID;

public record SessionStatusResource(SessionResource session, List<SubItemAttemptsResource> attemptedSubItems,
                                    List<UUID> pendingSubItemIds) {
}
