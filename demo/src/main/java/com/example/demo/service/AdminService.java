package com.example.demo.service;

import com.example.demo.enums.Status;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
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

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users_total", userRepository.count());
        stats.put("users_active", userRepository.countByStatus(Status.ACTIVE));
        stats.put("users_banned", userRepository.countByStatus(Status.BANNED));
        stats.put("posts_total", postRepository.count());
        stats.put("comments_total", commentRepository.count());
        return stats;
    }
}


