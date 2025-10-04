package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "chatroom")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;

    // ----- PHƯƠNG THỨC QUAN TRỌNG ĐƯỢC THÊM VÀO -----
    /**
     * Helper method để đồng bộ cả hai phía của mối quan hệ.
     * Vừa thêm user vào phòng chat, vừa thêm phòng chat vào cho user.
     */
    public void addParticipant(User user) {
        if (this.participants == null) {
            this.participants = new HashSet<>();
        }
        this.participants.add(user);

        if (user.getChatRooms() == null) {
            user.setChatRooms(new HashSet<>());
        }
        user.getChatRooms().add(this);
    }
}