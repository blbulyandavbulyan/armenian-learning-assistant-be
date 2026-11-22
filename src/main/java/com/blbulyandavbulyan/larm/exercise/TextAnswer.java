package com.blbulyandavbulyan.larm.exercise;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;

public record TextAnswer(String text) implements Answer {
    @Override
    public AnswerType type() {
        return AnswerType.TEXT;
    }
}
