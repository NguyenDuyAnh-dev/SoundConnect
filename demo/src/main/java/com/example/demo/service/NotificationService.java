package com.example.demo.service;

import com.example.demo.dto.response.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastNewPost(PostResponse postResponse) {
        // Gửi post mới cho tất cả client subscribe /topic/posts
        messagingTemplate.convertAndSend("/topic/posts", postResponse);
    }
}
