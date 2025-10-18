package com.example.demo.entity;

import com.example.demo.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "bands")
public class Band {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String name;
    String genre;
    String description;
    LocalDate createdAt;
    String avatar;
    String coverImage;
    Status status;
    // Một band có nhiều thành viên
    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL)
    @JsonIgnore
    List<BandMember> members;

    // Yêu cầu gia nhập band
    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL)
    @JsonIgnore
    List<BandJoinRequest> joinRequests;

    // Bài đăng của band (group-style)
    @OneToMany(mappedBy = "band", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Post> posts;

    // --- Followed by users ---
    @ManyToMany(mappedBy = "followedBands", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> followers;
}
