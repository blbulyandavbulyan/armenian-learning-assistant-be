package com.blbulyandavbulyan.larm.ai.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckSimilarPhrasesTool {
    @Tool
    public List<SimilarPhraseCheckResult> batchCheckSimilarPhrases(List<String> phrases) {

    }

}
