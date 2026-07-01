package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Search DialoguesResponse")
public record SearchDialoguesResponse(List<DialogueSummaryResponse> dialogues){
}
