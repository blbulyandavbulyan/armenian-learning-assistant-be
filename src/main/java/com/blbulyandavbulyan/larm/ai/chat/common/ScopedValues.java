package com.blbulyandavbulyan.larm.ai.chat.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ScopedValues {
    public static final ScopedValue<UUID> CONVERSATION_ID = ScopedValue.newInstance();
}
