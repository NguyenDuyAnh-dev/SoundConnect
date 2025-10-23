package com.example.demo.repository;


import com.example.demo.entity.Band;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BandRepository extends JpaRepository<Band, Integer> {
    // Lấy band đang hoạt động
    List<Band> findByStatus(Status status);

    // Tìm band theo tên (search)
    List<Band> findByNameContainingIgnoreCaseAndStatus(String keyword, Status status);

    Page<Band> findByStatus(Status status, Pageable pageable);
    Page<Band> findByNameContainingIgnoreCaseAndStatus(String name, Status status, Pageable pageable);
}
