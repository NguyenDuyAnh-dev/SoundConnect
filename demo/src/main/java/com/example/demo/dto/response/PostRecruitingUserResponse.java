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
public class PostRecruitingUserResponse {
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
    String playerName;        // tên người chơi
    String instrument;        // nhạc cụ đang chơi
    String playerExperience;  // kinh nghiệm
    String playerGenre;       // thể loại nhạc ưa thích
    String playerBio;         // giới thiệu ngắn gọn
    String bandRoles;        // Vai trò đang tuyển
    String hashtags;
}
