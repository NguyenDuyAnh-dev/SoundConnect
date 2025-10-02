package com.example.demo.dto.response;


import com.example.demo.enums.PostType;
import com.example.demo.enums.Status;
import com.example.demo.enums.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    Integer id;
    LocalDateTime postTime;
    UserResponse author;
    String content;
    String location;
    int comment;   // số lượng comment
    String media;
    int reaction;  // số lượng reaction
    Visibility visibility;
    PostType postType;
    Status status;
}

