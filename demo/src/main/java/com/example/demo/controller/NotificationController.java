package com.example.demo.controller;

import com.example.demo.dto.response.NotificationFCM;
import com.example.demo.service.NotificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @PostMapping("notify")
    public  void sendNotification(@RequestBody NotificationFCM notification) {
        // Implementation for sending notification
        notificationService.sendNotification(notification);
    }
}
