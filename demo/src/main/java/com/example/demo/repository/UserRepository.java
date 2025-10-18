package com.example.demo.repository;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    // Lấy danh sách user follow một band
    Page<User> findByFollowedBands_Id(Integer bandId, Pageable pageable);

    // Lấy danh sách user follow một venue
    Page<User> findByFollowedVenues_Id(Integer venueId, Pageable pageable);
    // Đếm số follower nhanh
    long countByFollowedBands_Id(Integer bandId);
    long countByFollowedVenues_Id(Integer venueId);
}
