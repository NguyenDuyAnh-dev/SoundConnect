package com.example.demo.dto.request;

import com.example.demo.enums.PostType;
import com.example.demo.enums.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRecruitingVenueRequest {
    String eventName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate eventDateTime;


    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime eventStartTime;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime eventEndTime;       // Thời gian biểu diễn tới mấy giờ
    String eventGenre;              // Thể loại mong muốn
    String eventScale;              // Quy mô: solo, 2 người, 3 người, 4+
    String eventDescription;        // Mô tả chi tiết
    String eventBenefits;           // Quyền lợi khác
    LocalDateTime eventDeadline;    // Hạn chót nhận hồ sơ
    LocalDateTime eventExpectedReply; // Thời gian phản hồi dự kiến
    String eventApplicationRequirement; // Yêu cầu khi ứng tuyển
    String bandExperience;   // Kinh nghiệm mong muốn
    String location;
    MultipartFile file;
    Visibility visibility;
    PostType postType;
}
