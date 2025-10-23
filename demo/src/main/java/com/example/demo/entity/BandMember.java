package com.example.demo.entity;

import com.example.demo.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "band_members")
public class BandMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "band_id")
    Band band;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    String roleInBand;
    LocalDateTime joinedAt;
    Status status;
}
