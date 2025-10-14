package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_posts")
@Data
@NoArgsConstructor
public class InstrumentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // Dùng cho các chuỗi văn bản dài
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // Mối quan hệ: Một người dùng có thể có nhiều bài đăng

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PostStatus {
        AVAILABLE, // Đang bán
        SOLD       // Đã bán
    }
}

