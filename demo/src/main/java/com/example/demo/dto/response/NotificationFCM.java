package com.example.demo.dto.response;

import lombok.Data;

@Data
public class NotificationFCM {
    String title;
    String message;
    String fcmToken;
}
