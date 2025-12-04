package com.example.demo.service;

import com.example.demo.enums.Status;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.SalePostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.InstrumentPostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService {
    UserRepository userRepository;
    PostRepository postRepository;
    CommentRepository commentRepository;
    SalePostRepository salePostRepository;
    InstrumentPostRepository instrumentPostRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Thống kê User
        stats.put("users_total", userRepository.count());
        stats.put("users_active", userRepository.countByStatus(Status.ACTIVE));
        stats.put("users_banned", userRepository.countByStatus(Status.BANNED));
        
        // Thống kê Post và Comment
        stats.put("posts_total", postRepository.count());
        stats.put("comments_total", commentRepository.count());
        
        // Thống kê SalePost và tổng tiền
        stats.put("sale_posts_total", salePostRepository.count());
        stats.put("sale_posts_active", salePostRepository.findByStatus(Status.ACTIVE).size());
        
        // Tính tổng tiền từ tất cả SalePost có status ACTIVE
        Double totalRevenue = salePostRepository.findByStatus(Status.ACTIVE)
                .stream()
                .mapToDouble(salePost -> {
                    if (salePost.getPrice() != null && salePost.getQuantity() != null) {
                        return salePost.getPrice() * salePost.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
        
        stats.put("total_revenue", totalRevenue);
        
        // Thống kê InstrumentPost
        stats.put("instrument_posts_total", instrumentPostRepository.count());
        long instrumentAvailable = instrumentPostRepository.findAll().stream()
                .filter(post -> post.getStatus() == com.example.demo.entity.InstrumentPost.PostStatus.AVAILABLE)
                .count();
        stats.put("instrument_posts_available", instrumentAvailable);
        
        // Tính tổng giá trị nhạc cụ đang bán
        Double totalInstrumentValue = instrumentPostRepository.findAll().stream()
                .filter(post -> post.getStatus() == com.example.demo.entity.InstrumentPost.PostStatus.AVAILABLE)
                .mapToDouble(post -> post.getPrice() != null ? post.getPrice().doubleValue() : 0.0)
                .sum();
        stats.put("total_instrument_value", totalInstrumentValue);
        
        return stats;
    }
}


