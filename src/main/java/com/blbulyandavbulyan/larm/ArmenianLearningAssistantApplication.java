package com.blbulyandavbulyan.larm;

import com.blbulyandavbulyan.larm.config.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(AppSecurityProperties.class)
@ConfigurationPropertiesScan
@SpringBootApplication
public class ArmenianLearningAssistantApplication {

    static void main(String[] args) {
        SpringApplication.run(ArmenianLearningAssistantApplication.class, args);
    }

}
