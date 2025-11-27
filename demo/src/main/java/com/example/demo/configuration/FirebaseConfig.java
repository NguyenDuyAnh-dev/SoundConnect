package com.example.demo.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Value("${fcm-credentials.file.path}")
    private String fcmCredentials;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Cấu hình FirebaseApp sử dụng fcmCredentials
        // Ví dụ: Tạo FirebaseOptions và khởi tạo FirebaseApp
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(fcmCredentials).getInputStream()))
                .build();
        return FirebaseApp.initializeApp(options); // Thay thế bằng FirebaseApp đã khởi tạo
    }
}
