package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByAuthor_Username(String username);
    List<Post> findByContentContaining(String content);

    List<Post> findByAuthor_UsernameOrderByPostTimeDesc(String username);

    List<Post> findByContentContainingIgnoreCase(String keyword);

    Page<Post> findByAuthor_UsernameOrderByPostTimeDesc(String username, Pageable pageable);

    Page<Post> findByContentContainingIgnoreCase(String keyword, Pageable pageable);

}
