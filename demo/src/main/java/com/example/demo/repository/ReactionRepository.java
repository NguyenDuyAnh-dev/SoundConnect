package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Integer> {
    Optional<Reaction> findByPostAndUser(Post post, User user);

    Page<Reaction> findByPost_IdAndStatus(Integer postId, Status status, Pageable pageable);

    Long countByPostAndStatus(Post post, Status status);
}
