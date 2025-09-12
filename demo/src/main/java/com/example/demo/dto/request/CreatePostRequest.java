package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank(message = "Content cannot be empty") String content,
        String metadata,
        String visibility // PUBLIC / FRIENDS / PRIVATE
) {}