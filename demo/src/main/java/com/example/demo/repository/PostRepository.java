package com.example.demo.repository;

import com.example.demo.entity.Band;
import com.example.demo.entity.Post;
import com.example.demo.entity.Venue;
import com.example.demo.enums.Status;
import com.example.demo.enums.Visibility;
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

//    Page<Post> findByAuthor_UsernameAndAvailableTrueOrderByPostTimeDesc(String username, Pageable pageable);
//
//    Page<Post> findByContentContainingIgnoreCaseAndAvailableTrue(String keyword, Pageable pageable);
//
//    Page<Post> findByAvailableTrue(Pageable pageable);

    // Lấy post của user
    Page<Post> findByAuthor_UsernameAndStatusOrderByPostTimeDesc(
            String username, Status status, Pageable pageable);

    // Search content
    Page<Post> findByContentContainingIgnoreCaseAndStatus(
            String keyword, Status status, Pageable pageable);

    // Lấy tất cả post active
    Page<Post> findByStatus(Status status, Pageable pageable);

    Page<Post> findByBandAndStatusAndVisibility(Band band, Status status, Visibility visibility, Pageable pageable);

    Page<Post> findByVenueAndStatusAndVisibility(Venue venue, Status status, Visibility visibility, Pageable pageable);


}
