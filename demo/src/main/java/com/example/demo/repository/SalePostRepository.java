package com.example.demo.repository;

import com.example.demo.entity.SalePost;
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
}
