package com.blbulyandavbulyan.larm.exercise;

import com.blbulyandavbulyan.larm.dao.entities.AnswerType;

/**
 * An answer given by the user; one implementation per {@link AnswerType}.
 */
public sealed interface Answer permits TextAnswer {
    AnswerType type();
}
