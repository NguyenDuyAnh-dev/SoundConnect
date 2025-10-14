package com.example.demo.service;

import com.example.demo.dto.response.SalePostForCategoryPageResponse;
import com.example.demo.dto.response.SalePostForCategoryResponse;
import com.example.demo.dto.response.SalePostPageResponse;
import com.example.demo.dto.response.SalePostResponse;
import com.example.demo.entity.SalePost;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.SalePostMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.SalePostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    SalePostMapper salePostMapper;

    public SalePostPageResponse getAllSalePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<SalePost> salePostPage = salePostRepository.findByStatus(Status.ACTIVE, pageable);

        List<SalePostResponse> content = salePostMapper.toSalePostResponses(salePostPage.getContent());

        SalePostPageResponse response = new SalePostPageResponse();
        response.setContent(content);
        response.setPageNumber(salePostPage.getNumber()); // +1 để client thấy page bắt đầu từ 1
        response.setTotalElements(salePostPage.getTotalElements());
        response.setTotalPages(salePostPage.getTotalPages());

        return response;
    }

    public SalePostResponse getSalePostById(Integer id) {
        SalePost salePost = salePostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Chỉ cho phép lấy post ACTIVE
        if (salePost.getStatus() != Status.ACTIVE) {
            throw new AppException(ErrorCode.POST_NOT_EXISTED);
        }

        return salePostMapper.toSalePostResponse(salePost);
    }


    public SalePostForCategoryPageResponse getSalePostsByCategory(Integer categoryId, int page, int size) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));


        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<SalePost> salePostPage = salePostRepository.findByCategory_Id(categoryId, pageable);

        List<SalePostForCategoryResponse> content = salePostMapper.toSalePostForCategoryResponses(salePostPage.getContent());

        SalePostForCategoryPageResponse salePostForCategoryPageResponse = new SalePostForCategoryPageResponse();
        salePostForCategoryPageResponse.setContent(content);
        salePostForCategoryPageResponse.setPageNumber(salePostPage.getNumber());
        salePostForCategoryPageResponse.setTotalPages(salePostPage.getTotalPages());
        salePostForCategoryPageResponse.setTotalElements(salePostPage.getTotalElements());

        return salePostForCategoryPageResponse;
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
