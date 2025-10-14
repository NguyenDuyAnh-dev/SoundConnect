package com.example.demo.repository;

import com.example.demo.entity.Venue;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Integer> {

    // Lấy tất cả venue theo trạng thái
    List<Venue> findByStatus(Status status);

    // Tìm theo tên (không phân biệt hoa thường) và trạng thái
    List<Venue> findByNameContainingIgnoreCaseAndStatus(String name, Status status);

    // Lấy theo chủ sở hữu
    List<Venue> findByOwnerId(String ownerId);

    Page<Venue> findByStatus(Status status, Pageable pageable);
    Page<Venue> findByNameContainingIgnoreCaseAndStatus(String name, Status status, Pageable pageable);
    Page<Venue> findByOwner_UsernameAndStatus(String username, Status status, Pageable pageable);


}
