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
public class PostPageResponse {
    Integer id;
    String author;
    String userId;
    String username;
    String userAvatar;
    Integer bandId;
    Integer venueId;
    String content;
    String location;
    String media;
    LocalDateTime postTime;
    int reactionCount;
    int commentCount;
}
