package com.example.demo.dto.request;

import lombok.Data;

@Data
public class FollowRequest {
    private String userId; // ID của user đang follow/unfollow
}
