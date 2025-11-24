package com.example.demo.entity;

import com.example.demo.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "userId")
    private String id;

     String username;
     String name;
     String password;
     String firstname;
     String lastname;
     LocalDate dob;
     Status status;
    private String fcmToken; // để biết ai sẽ đc push thông báo
    private String hometown;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String avatar;

    private String gender;
    private String bio;
    @OneToMany
    private List<User> favorList;
    @OneToMany
    private List<User> disfavorList;
    @OneToMany
    private List<User> befavoredList;
    @OneToMany
    private List<User> bedisfavoredList;
    @OneToMany
    private List<Notification> notificationList;
    @Builder.Default // Quan trọng khi dùng @Builder
    @Column(precision = 19, scale = 2) // Định nghĩa kiểu dữ liệu trong DB
    private BigDecimal balance = BigDecimal.ZERO; // Khai báo biến và gán giá trị mặc định
    private Boolean available;
        @ManyToMany()
     Set<Role> roles;

// --- Mối quan hệ với các Entity khác trong hệ thống Chat ---
    // Quan hệ
    @OneToMany(mappedBy = "author")
    @JsonIgnore
    List<Post> posts;

    @OneToMany(mappedBy = "author")
    @JsonIgnore
    List<Comment> comments;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @JsonIgnore
    List<SalePost> salePosts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    List<BandMember> bandMemberships;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    List<BandJoinRequest> joinRequests;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Venue> venues;

    // --- Follow Bands & Venues ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_follow_bands",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "band_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Band> followedBands;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_follow_venues",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "venue_id")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Venue> followedVenues;

    /**
     * Các phòng chat mà người dùng này tham gia.
     * `mappedBy = "participants"` chỉ ra rằng mối quan hệ này được quản lý bởi
     * trường `participants` trong entity `ChatRoom`.
     */
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude // Tránh vòng lặp vô hạn khi dùng equals/hashCode
    @ToString.Exclude // Tránh vòng lặp vô hạn khi dùng toString
    private Set<ChatRoom> chatRooms;

    /**
     * Các tin nhắn mà người dùng này đã gửi.
     */
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Message> sentMessages;
}