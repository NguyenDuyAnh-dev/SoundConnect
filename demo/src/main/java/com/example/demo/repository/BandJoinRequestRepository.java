package com.example.demo.repository;


import com.example.demo.entity.BandJoinRequest;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BandJoinRequestRepository extends JpaRepository<BandJoinRequest, Integer> {

    // Lấy yêu cầu theo band
    List<BandJoinRequest> findByBandId(Integer bandId);

    // Lấy yêu cầu của 1 user gửi đến band cụ thể
    Optional<BandJoinRequest> findByUser_UsernameAndBandId(String username, Integer bandId);

    // Lấy tất cả yêu cầu đang chờ của 1 band
    List<BandJoinRequest> findByBandIdAndStatus(Integer bandId, Status status);

    Page<BandJoinRequest> findByBandId(Integer bandId, Pageable pageable);
    Page<BandJoinRequest> findByUser_Username(String username, Pageable pageable);
}
