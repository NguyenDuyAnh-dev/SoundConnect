package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReactionResponse {
    Integer id;
    String userId;
    String username;
    String avatar;
    String type;           // LIKE, DISLIKE
    LocalDateTime createdAt;
}
