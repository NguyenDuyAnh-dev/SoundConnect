package com.example.demo.configuration;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${spring.ai.gemini.api-key}")
    private String apiKey;

    @Bean
    public Client AIClient() {
        return Client.builder()
                .apiKey(apiKey)  // lấy từ application.properties
                .build();
    }
}
