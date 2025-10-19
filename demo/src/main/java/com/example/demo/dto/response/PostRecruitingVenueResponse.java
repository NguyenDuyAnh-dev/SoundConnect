package com.example.demo.dto.response;

import com.example.demo.enums.PostType;
import com.example.demo.enums.Status;
import com.example.demo.enums.Visibility;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRecruitingVenueResponse {
    Integer id;
    LocalDateTime postTime;
    UserResponse author;
    VenueResponse venue;
    String location;
    int comment;   // số lượng comment
    String media;
    int reaction;  // số lượng reaction
    Visibility visibility;
    PostType postType;
    Status status;
    String eventName;
    LocalDate eventDateTime;    // Ngày và thời gian biểu diễn
    LocalTime eventStartTime;       // Thời gian biểu diễn từ mấy giờ
    LocalTime eventEndTime;         // Thời gian biểu diễn tới mấy giờ
    String eventGenre;              // Thể loại mong muốn
    String eventScale;              // Quy mô: solo, 2 người, 3 người, 4+
    String eventDescription;        // Mô tả chi tiết
    String eventBenefits;           // Quyền lợi khác
    LocalDateTime eventDeadline;    // Hạn chót nhận hồ sơ
    LocalDateTime eventExpectedReply; // Thời gian phản hồi dự kiến
    String eventApplicationRequirement; // Yêu cầu khi ứng tuyển
    String bandExperience;   // Kinh nghiệm mong muốn
}
