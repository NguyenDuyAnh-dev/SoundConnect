package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // Lấy tất cả comment theo postId trực tiếp
    List<Comment> findByPost_Id(Integer postId);

    Page<Comment> findByPost_Id(Integer postId, Pageable pageable);
}
