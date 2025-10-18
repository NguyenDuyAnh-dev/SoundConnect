package com.example.demo.entity;

import com.example.demo.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String name;
    String location;
    String description;
    String coverImage;
    String avatarImage;
    String contactInfo;
    Status status;

    // Các bài đăng của phòng trà (page-style)
    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Post> posts;

    // Thêm quan hệ: Mỗi venue thuộc về 1 user (chủ phòng trà)
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    // --- Followed by users ---
    @ManyToMany(mappedBy = "followedVenues", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> followers;

}
