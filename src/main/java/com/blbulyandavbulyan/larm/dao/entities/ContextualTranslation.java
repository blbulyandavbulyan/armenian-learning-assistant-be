package com.blbulyandavbulyan.larm.dao.entities;

import java.util.UUID;

public interface ContextualTranslation {

    UUID getId();

    String getIsoLanguageCode();

    String getTranslationText();
}
