package com.blbulyandavbulyan.larm.ai;

public interface TextToSpeechService {

    SpeechResource convert(String text, String isoLanguageCode);
}
