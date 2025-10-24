package com.example.demo.repository;

import com.example.demo.entity.BandMember;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BandMemberRepository extends JpaRepository<BandMember, Integer> {
    // Lấy danh sách thành viên trong band (chỉ ACTIVE)
    List<BandMember> findByBand_IdAndStatus(Integer bandId, Status status);

    // Kiểm tra 1 user có đang là thành viên của band không
    Optional<BandMember> findByBand_IdAndUser_Username(Integer bandId, String username);

    // Lấy tất cả band mà user đang tham gia
    List<BandMember> findByUser_UsernameAndStatus(String username, Status status);

    // Lấy leader của band
    Optional<BandMember> findByBand_IdAndRoleInBandAndStatus(Integer bandId, String roleInBand, Status status);

    Page<BandMember> findByBand_IdAndStatus(Integer bandId, Status status, Pageable pageable);

    Page<BandMember> findByUser_UsernameAndStatus(String username, Status status, Pageable pageable);
}
