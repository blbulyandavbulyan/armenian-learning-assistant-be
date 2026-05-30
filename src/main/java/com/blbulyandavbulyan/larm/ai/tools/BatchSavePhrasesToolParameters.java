package com.blbulyandavbulyan.larm.ai.tools;

import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

public record BatchSavePhrasesToolParameters(
        @ToolParam(description = "List of create phrases requests")
        List<CreatePhraseToolParameters> createPhrases) {
}
