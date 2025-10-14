package com.example.demo.dto.response;

import com.example.demo.dto.request.UserDTO;
import com.example.demo.entity.InstrumentPost;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InstrumentPostResponse {
    Long id;
    String title;
    String description;
    BigDecimal price;
    String imageUrl;
    InstrumentPost.PostStatus status;
    LocalDateTime createdAt;
    UserDTO seller; // Chứa thông tin người bán
}
