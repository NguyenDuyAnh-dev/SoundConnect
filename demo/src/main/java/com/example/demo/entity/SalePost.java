package com.example.demo.entity;

import com.example.demo.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "sale_posts")
public class SalePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    Double price;
    Integer quantity;
    String location;

    Double commissionRate; // % hoa hồng

    Status status;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    // Quan hệ: 1 SalePost có nhiều SaleImage
    @OneToMany(mappedBy = "salePost", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SaleImage> images;

    // Quan hệ: nhiều SalePost thuộc 1 Category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
