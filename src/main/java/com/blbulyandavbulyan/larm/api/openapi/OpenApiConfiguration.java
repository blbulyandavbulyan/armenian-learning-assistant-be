package com.blbulyandavbulyan.larm.api.openapi;

import java.util.Map;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenApiCustomizer cleanSpringBoot4SpecCustomizer() {
        return openApi -> {
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                Map<String, Schema> schemas = openApi.getComponents().getSchemas();

                schemas.forEach((name, schema) -> {
                    // Forcefully clear the default 'null' if Jackson 3 flagged the record object as null
                    if ("object".equals(schema.getType())) {
                        schema.setDefault(null);
                    }

                    // Clean up properties inside your DTOs/Records
                    if (schema.getProperties() != null) {
                        Map<String, Schema> properties = schema.getProperties();
                        properties.forEach((propName, propSchema) -> {
                            // Fix the arrays that are breaking with contains/unevaluatedItems defaults
                            if ("array".equals(propSchema.getType())) {
                                propSchema.setContains(null);
                                propSchema.setUnevaluatedItems(null);
                            }
                        });
                    }
                });
            }
        };
    }

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Armenian Learning Assistant API")
                        .version("0.0.1")
                        .description("Backend service for generating and managing Armenian learning phrases using AI and TTS."));
    }
}
