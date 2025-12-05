package com.example.demo.dto.response;

import lombok.Data;
import java.util.Set;

@Data
public class ChatRoomResponse {
    private Long id;
    private String name;   // Tên hiển thị (sẽ được xử lý logic)
    private String avatar; // Ảnh hiển thị (sẽ được xử lý logic)
    private String type;   // ONE_ON_ONE hoặc GROUP

    // Giữ nguyên list participants để Frontend dùng nếu cần
    private Set<UserResponse> participants;
}