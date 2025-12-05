package com.example.demo.entity;

import com.example.demo.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // === BỔ SUNG CÁI NÀY ===
    @Enumerated(EnumType.STRING) // Lưu xuống DB dưới dạng chữ ("ONE_ON_ONE") cho dễ đọc
    private RoomType type;
    // =======================

    @ManyToMany
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants;

    // ... các trường khác (createdAt, v.v.)
}