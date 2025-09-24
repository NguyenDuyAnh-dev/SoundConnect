package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Integer> {
    // Lấy danh sách like theo Post
    List<Reaction> findByPost_Id(Integer postId);

    // Kiểm tra user đã like post chưa
    Optional<Reaction> findByPostAndUser(Post post, User user);

    // Đếm số like của 1 post
    Long countByPost(Post post);

    //Dùng Pageable của Spring Data, không phải java.awt
    Page<Reaction> findByPost_Id(Integer postId, Pageable pageable);
}
