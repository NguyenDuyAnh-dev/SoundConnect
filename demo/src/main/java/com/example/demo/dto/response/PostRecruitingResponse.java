package com.example.demo.dto.response;

import com.example.demo.enums.PostType;
import com.example.demo.enums.Status;
import com.example.demo.enums.Visibility;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRecruitingResponse {
    Integer id;
    LocalDateTime postTime;
    UserResponse author;
    String location;
    int comment;   // số lượng comment
    String media;
    int reaction;  // số lượng reaction
    Visibility visibility;
    PostType postType;
    Status status;
    String bandName;
    String bandGenre;
    String bandDescription;
    String bandRoles;        // Vai trò đang tuyển
    String bandExperience;   // Kinh nghiệm mong muốn
    String hashtags;
}
