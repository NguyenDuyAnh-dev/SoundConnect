package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowerResponse {
    private String userId;
    private String username;
    private String name;
    private String avatar;
}
