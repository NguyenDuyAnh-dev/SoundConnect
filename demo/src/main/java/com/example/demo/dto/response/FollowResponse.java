package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponse {
    private String message;
    private int totalFollowers; // Số người follow hiện tại
}
