package com.example.demo.entity;

import com.example.demo.enums.Status;
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

    @Builder.Default
    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

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
    private Boolean available;


        @ManyToMany()
     Set<Role> roles;

// --- Mối quan hệ với các Entity khác trong hệ thống Chat ---

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