package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import lombok.Builder;

@Builder
record PhraseChatResponse(String message, List<DraftPhraseResponse> phrases) {
}
