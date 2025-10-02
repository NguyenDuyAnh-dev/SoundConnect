package com.example.demo.service;

import com.example.demo.dto.response.CommentDTO;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.dto.response.ReactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;


    // ----------------- POST -----------------
    public void broadcastNewPost(PostResponse postResponse) {
        // Gửi post mới cho tất cả client subscribe /topic/posts
        messagingTemplate.convertAndSend("/topic/posts", postResponse);
    }

    // Gửi post đã update
    public void broadcastUpdatedPost(PostResponse postResponse) {
        messagingTemplate.convertAndSend("/topic/posts/update", postResponse);
    }

    // Gửi sự kiện post bị xoá mềm
    public void broadcastDeletedPost(PostResponse postResponse) {
        messagingTemplate.convertAndSend("/topic/posts/delete", postResponse);
    }

    // ----------------- COMMENT -----------------
    public void broadcastNewComment(int postId, CommentDTO commentDTO) {
        // Gửi comment mới tới channel của post cụ thể
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments", commentDTO);
    }

    // Broadcast comment đã update
    public void broadcastUpdatedComment(int postId, CommentDTO commentDTO) {
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments/update", commentDTO);
    }

    // Broadcast comment đã xoá
    public void broadcastDeletedComment(int postId, CommentDTO commentDTO) {
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments/delete", commentDTO);
    }

    // ----------------- REACTION -----------------
    public void broadcastNewReaction(int postId, ReactionResponse reactionDTO) {
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/reactions", reactionDTO);
    }

    public void broadcastDeletedReaction(int postId, ReactionResponse reactionDTO) {
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/reactions/delete", reactionDTO);
    }

}
