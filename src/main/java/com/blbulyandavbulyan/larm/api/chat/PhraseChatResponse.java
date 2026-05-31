package com.blbulyandavbulyan.larm.api.chat;

import lombok.Builder;

import java.util.List;

@Builder
record PhraseChatResponse(String message, List<DraftPhraseResponse> phrases) {
}
