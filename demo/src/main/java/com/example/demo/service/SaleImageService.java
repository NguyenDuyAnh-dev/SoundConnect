package com.example.demo.service;

import com.example.demo.entity.SaleImage;
import com.example.demo.repository.SaleImageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaleImageService {
    @Autowired
    SaleImageRepository saleImageRepository;

    public List<SaleImage> getAllSaleImages() {
        return saleImageRepository.findAll();
    }

    public Optional<SaleImage> getSaleImageById(Integer id) {
        return saleImageRepository.findById(id);
    }

    public List<SaleImage> getImagesBySalePost(Integer salePostId) {
        return saleImageRepository.findBySalePost_Id(salePostId);
    }

    public SaleImage getPrimaryImageBySalePost(Integer salePostId) {
        return saleImageRepository.findBySalePost_IdAndIsPrimaryTrue(salePostId);
    }

    public SaleImage createSaleImage(SaleImage saleImage) {
        return saleImageRepository.save(saleImage);
    }

    public SaleImage updateSaleImage(Integer id, SaleImage saleImageDetails) {
        return saleImageRepository.findById(id).map(image -> {
            image.setImageUrl(saleImageDetails.getImageUrl());
            image.setIsPrimary(saleImageDetails.getIsPrimary());
            image.setCreatedAt(saleImageDetails.getCreatedAt());
            image.setSalePost(saleImageDetails.getSalePost());
            return saleImageRepository.save(image);
        }).orElseThrow(() -> new RuntimeException("SaleImage not found with id " + id));
    }

    public void deleteSaleImage(Integer id) {
        saleImageRepository.deleteById(id);
    }
}
