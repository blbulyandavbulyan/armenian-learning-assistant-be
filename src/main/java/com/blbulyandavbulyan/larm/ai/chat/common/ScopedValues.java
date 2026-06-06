package com.blbulyandavbulyan.larm.ai.chat.common;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScopedValues {
    public static final ScopedValue<UUID> CONVERSATION_ID = ScopedValue.newInstance();
}
