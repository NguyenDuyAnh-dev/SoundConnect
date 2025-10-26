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

    private String imageUrl; // URL đến ảnh nhạc cụ (nếu có)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.AVAILABLE; // Mặc định là đang bán

    @ManyToOne(fetch = FetchType.LAZY) // Fetch LAZY để tối ưu hiệu năng
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller; // Liên kết với người bán

    @CreationTimestamp // Tự động ghi thời gian tạo
    private LocalDateTime createdAt;

    @UpdateTimestamp // Tự động ghi thời gian cập nhật
    private LocalDateTime updatedAt;

    // Trạng thái của bài đăng
    public enum PostStatus {
        AVAILABLE, // Đang bán
        SOLD       // Đã bán
    }
}

