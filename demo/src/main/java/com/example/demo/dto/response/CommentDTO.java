package com.example.demo.dto.response;


import com.example.demo.entity.Post;
import com.example.demo.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDTO {
    String avatar;
    String authorName;
    String content;
    LocalDateTime commentTime;
    Post post;
    Status status;
}
