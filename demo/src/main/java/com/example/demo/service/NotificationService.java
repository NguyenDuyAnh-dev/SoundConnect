package com.example.demo.service;

import com.example.demo.dto.response.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public FirebaseMessaging firebaseMessaging;

    public NotificationService(FirebaseApp firebaseApp, SimpMessagingTemplate messagingTemplate) {
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
        this.messagingTemplate = messagingTemplate;

    }

    public void sendNotification(NotificationFCM notification) {
        Notification firebaseNotification = Notification.builder()
                .setTitle(notification.getTitle())
                .setBody(notification.getMessage())
                .build();

        Message message = Message.builder()
                .setToken(notification.getFcmToken())
                .setNotification(firebaseNotification)
                .build();
        try{
            firebaseMessaging.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


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

    // ----------------- SALE POST-----------------
    // Gửi thông báo khi có bài đăng mới
    public void sendNewSalePost(SalePostResponse salePostResponse) {
        messagingTemplate.convertAndSend("/topic/sale-posts", salePostResponse);
    }

    // Gửi thông báo khi có bài đăng được cập nhật
    public void sendUpdatedSalePost(SalePostResponse salePostResponse) {
        messagingTemplate.convertAndSend("/topic/sale-posts/update", salePostResponse);
    }

    // Gửi thông báo khi có bài đăng bị xóa
    public void sendDeletedSalePost(Integer postId) {
        messagingTemplate.convertAndSend("/topic/sale-posts/delete", postId);
    }

    // ----------------- SALE IMAGE -----------------
    public void sendNewSaleImage(Integer salePostId, SaleImageResponse imageResponse) {
        messagingTemplate.convertAndSend("/topic/sale-posts/" + salePostId + "/images", imageResponse);
    }

    public void sendUpdatedSaleImage(Integer salePostId, SaleImageResponse imageResponse) {
        messagingTemplate.convertAndSend("/topic/sale-posts/" + salePostId + "/images/update", imageResponse);
    }

    public void sendDeletedSaleImage(Integer salePostId, Integer imageId) {
        messagingTemplate.convertAndSend("/topic/sale-posts/" + salePostId + "/images/delete", imageId);
    }

    // ----------------- CATEGORY -----------------
    public void sendNewCategory(CategoryResponse categoryResponse) {
        messagingTemplate.convertAndSend("/topic/categories", categoryResponse);
    }

    public void sendUpdatedCategory(CategoryResponse categoryResponse) {
        messagingTemplate.convertAndSend("/topic/categories/update", categoryResponse);
    }

    public void sendDeletedCategory(Integer categoryId) {
        messagingTemplate.convertAndSend("/topic/categories/delete", categoryId);
    }

}
