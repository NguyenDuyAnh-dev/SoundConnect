package com.example.demo.dto.request;

// CreateShareRequest.java
public record CreateShareRequest(
        Long originalPostId,
        String content // caption tuỳ chọn
) {}