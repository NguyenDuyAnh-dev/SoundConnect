package com.example.demo.dto.request;

import com.example.demo.enums.PostType;
import com.example.demo.enums.Visibility;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRecruitingUserRequest {
    String playerName;        // tên người chơi
    String instrument;        // nhạc cụ đang chơi
    String playerExperience;  // kinh nghiệm
    String playerGenre;       // thể loại nhạc ưa thích
    String playerBio;         // giới thiệu ngắn gọn
    String bandRoles;        // Vai trò đang tuyển
    String hashtags;
    String location;
    MultipartFile file;
    Visibility visibility;
    PostType postType;
}
