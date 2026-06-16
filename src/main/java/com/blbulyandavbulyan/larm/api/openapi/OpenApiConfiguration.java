package com.blbulyandavbulyan.larm.api.openapi;

import java.util.List;
import java.util.Map;

import com.blbulyandavbulyan.larm.api.chat.TranslationResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Armenian Learning Assistant API")
                        .version("0.0.1")
                        .description("Backend service for generating and managing Armenian learning phrases using AI and TTS."));
    }

    @Bean
    public OpenApiCustomizer fieldExampleCustomizer() {
        return openApi -> {
            var schemas = openApi.getComponents().getSchemas();
            if (schemas == null) {
                return;
            }

            // 1. Set the example for DialogueTitleResponse's translations
            setTranslationExample(schemas, OpenApiConstants.SchemaNames.DIALOGUE_TITLE_RESPONSE,
                    OpenApiConstants.PropertyNames.TRANSLATIONS, TranslationResponse.builder()
                            .translationText(OpenApiConstants.Examples.DIALOGUE_TRANSLATION_TEXT)
                            .isoLanguageCode(OpenApiConstants.Examples.TRANSLATION_ISO_LANGUAGE_CODE)
                            .build());

            // 2. Set the example for SpeakerResponse's translations
            setTranslationExample(schemas, OpenApiConstants.SchemaNames.SPEAKER_RESPONSE,
                    OpenApiConstants.PropertyNames.TRANSLATIONS, TranslationResponse.builder()
                            .translationText(OpenApiConstants.Examples.SPEAKER_TRANSLATION_TEXT)
                            .isoLanguageCode(OpenApiConstants.Examples.TRANSLATION_ISO_LANGUAGE_CODE)
                            .build());

            // 3. Set the example for DraftPhraseResponse's translations
            setTranslationExample(schemas, OpenApiConstants.SchemaNames.DRAFT_PHRASES_RESPONSE,
                    OpenApiConstants.PropertyNames.TRANSLATIONS, TranslationResponse.builder()
                            .translationText(OpenApiConstants.Examples.TRANSLATION_TEXT)
                            .isoLanguageCode(OpenApiConstants.Examples.TRANSLATION_ISO_LANGUAGE_CODE)
                            .build());
        };
    }

    private void setTranslationExample(Map<String, Schema> schemas, String schemaName, String propertyName, Object example) {
        var schema = schemas.get(schemaName);
        if (schema != null && schema.getProperties() != null) {
            var translationsSchema = (Schema<?>) schema.getProperties().get(propertyName);
            if (translationsSchema == null) {
                throw new IllegalStateException("Property '%s' not found in schema %s"
                        .formatted(propertyName, schemaName));
            }
            translationsSchema.setExample(List.of(example));
        }
    }
}
