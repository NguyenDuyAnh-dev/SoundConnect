package com.example.demo.entity;

import com.example.demo.enums.PostType;
import com.example.demo.enums.Status;
import com.example.demo.enums.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    Visibility visibility;

    PostType postType;

    @ManyToOne
    @JoinColumn(name = "band_id")
    Band band;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    Venue venue;


    String content;
    String location;
    String media;

//    Boolean available;
    Status status;

    // --- Thông tin band copy sang Post ---
    String bandName;
    String bandGenre;
    String bandDescription;
    String bandRoles;        // Vai trò đang tuyển
    String bandExperience;   // Kinh nghiệm mong muốn
    String hashtags;

    // --- Thông tin người chơi nhạc cụ (author) copy sang Post ---
    String playerName;        // tên người chơi
    String instrument;        // nhạc cụ đang chơi
    String playerExperience;  // kinh nghiệm
    String playerGenre;       // thể loại nhạc ưa thích
    String playerBio;         // giới thiệu ngắn

    // --- Thông tin Event copy sang Post ---
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


    LocalDateTime postTime;

    // Một post có nhiều comment
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<Comment> commentList = new ArrayList<>();

    // Một post có nhiều reaction
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<Reaction> reactions = new ArrayList<>();
}
