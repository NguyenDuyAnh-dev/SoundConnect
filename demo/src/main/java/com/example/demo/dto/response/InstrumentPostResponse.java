package com.example.demo.dto.response;

import com.example.demo.dto.request.UserDTO;
import com.example.demo.entity.InstrumentPost;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO dùng để trả về thông tin bài đăng cho client
@Data
public class InstrumentPostResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private InstrumentPost.PostStatus status;
    private LocalDateTime createdAt;
    // Bao gồm cả thông tin người bán để client biết cách liên hệ
    private UserDTO seller;
}

