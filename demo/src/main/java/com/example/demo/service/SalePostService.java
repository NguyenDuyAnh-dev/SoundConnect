package com.example.demo.service;

import com.example.demo.entity.SalePost;
import com.example.demo.repository.SalePostRepository;
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
public class SalePostService {
    @Autowired
    SalePostRepository salePostRepository;

    public List<SalePost> getAllSalePosts() {
        return salePostRepository.findAll();
    }

    public Optional<SalePost> getSalePostById(Integer id) {
        return salePostRepository.findById(id);
    }

    public SalePost createSalePost(SalePost salePost) {
        salePost.setCreatedAt(java.time.LocalDateTime.now());
        salePost.setUpdatedAt(java.time.LocalDateTime.now());
        return salePostRepository.save(salePost);
    }

    public SalePost updateSalePost(Integer id, SalePost updatedSalePost) {
        return salePostRepository.findById(id)
                .map(salePost -> {
                    salePost.setTitle(updatedSalePost.getTitle());
                    salePost.setDescription(updatedSalePost.getDescription());
                    salePost.setPrice(updatedSalePost.getPrice());
                    salePost.setQuantity(updatedSalePost.getQuantity());
                    salePost.setLocation(updatedSalePost.getLocation());
                    salePost.setCommissionRate(updatedSalePost.getCommissionRate());
                    salePost.setStatus(updatedSalePost.getStatus());
                    salePost.setIsActive(updatedSalePost.getIsActive());
                    salePost.setUpdatedAt(java.time.LocalDateTime.now());
                    salePost.setCategory(updatedSalePost.getCategory());
                    return salePostRepository.save(salePost);
                })
                .orElseThrow(() -> new RuntimeException("SalePost not found with id " + id));
    }

    public void deleteSalePost(Integer id) {
        salePostRepository.deleteById(id);
    }
}
