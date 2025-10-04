package com.example.demo.repository;

import com.example.demo.entity.SalePost;
import com.example.demo.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalePostRepository extends JpaRepository<SalePost, Integer> {
    // Lấy danh sách theo category
    List<SalePost> findByCategoryId(Integer categoryId);

    // Lấy danh sách theo user
    List<SalePost> findByAuthorId(String userId);

    // Lấy danh sách theo status
    List<SalePost> findByStatus(Enum status);

    Page<SalePost> findByCategory_Id(Integer categoryId, Pageable pageable);
    Page<SalePost> findByStatus(Status status, Pageable pageable);
}
