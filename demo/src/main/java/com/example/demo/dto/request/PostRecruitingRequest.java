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
public class PostRecruitingRequest {
    String bandName;
    String bandGenre;
    String bandDescription;
    String bandRoles;        // Vai trò đang tuyển
    String bandExperience;   // Kinh nghiệm mong muốn
    String hashtags;
    String location;
    MultipartFile file;
    Visibility visibility;
    PostType postType;
}
