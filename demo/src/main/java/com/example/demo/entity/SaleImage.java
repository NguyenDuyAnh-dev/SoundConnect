package com.example.demo.entity;

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
@Table(name = "sale_image")
public class SaleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String imageUrl;
    private Boolean isPrimary; // co dc set la anh dai dien cho cac anh khac hay ko
    private LocalDateTime createdAt;

    // Quan hệ: nhiều ảnh thuộc 1 SalePost
    @ManyToOne
    @JoinColumn(name = "sale_post_id")
    private SalePost salePost;
}
