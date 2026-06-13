package com.blbulyandavbulyan.larm.api.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
}
