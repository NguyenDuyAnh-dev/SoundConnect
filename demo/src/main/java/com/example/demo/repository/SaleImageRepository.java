package com.example.demo.repository;

import com.example.demo.entity.SaleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleImageRepository extends JpaRepository<SaleImage, Integer> {
    // Lấy tất cả ảnh theo SalePost
    List<SaleImage> findBySalePost_Id(Integer salePostId);

    // Lấy ảnh đại diện của 1 SalePost
    SaleImage findBySalePost_IdAndIsPrimaryTrue(Integer salePostId);
}