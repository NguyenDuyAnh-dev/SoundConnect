package com.example.demo.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "diyvmhvog",
                "api_key", "346787231424426",
                "api_secret", "cgykaFsfp5g6eIrBdDR7nJvshXA"
        ));
    }
}
