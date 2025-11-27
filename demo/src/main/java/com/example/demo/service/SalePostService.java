package com.example.demo.service;

import com.example.demo.dto.request.SalePostCreateRequest;
import com.example.demo.dto.request.SalePostUpdateRequest;
import com.example.demo.dto.response.*;
import com.example.demo.entity.Category;
import com.example.demo.entity.SalePost;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.SalePostMapper;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.SalePostRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationService notificationService;


    /** Lấy tất cả bài đăng (ACTIVE) có phân trang */
    public SalePostPageResponse getAllSalePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<SalePost> salePostPage = salePostRepository.findByStatus(Status.ACTIVE, pageable);

        List<SalePostResponse> content = salePostPage.getContent().stream().map(salePost -> {
            SalePostResponse dto = new SalePostResponse();
            dto.setId(salePost.getId());
            dto.setTitle(salePost.getTitle());
            dto.setDescription(salePost.getDescription());
            dto.setPrice(salePost.getPrice());
            dto.setQuantity(salePost.getQuantity());
            dto.setLocation(salePost.getLocation());
            dto.setCommissionRate(salePost.getCommissionRate());
            dto.setStatus(salePost.getStatus());
            dto.setCreatedAt(salePost.getCreatedAt());
            dto.setUpdatedAt(salePost.getUpdatedAt());

            // Map Category
            if (salePost.getCategory() != null) {
                dto.setCategoryId(salePost.getCategory().getId());
            }

            // Map Author
            if (salePost.getAuthor() != null) {
                dto.setAuthorId(String.valueOf(salePost.getAuthor().getId()));
                dto.setAuthorUsername(salePost.getAuthor().getUsername());
                dto.setAuthorAvatar(salePost.getAuthor().getAvatar());
            }

            // Map Images
            if (salePost.getImages() != null) {
                dto.setImages(
                        salePost.getImages().stream().map(image -> {
                            SaleImageResponse imgDto = new SaleImageResponse();
                            imgDto.setId(image.getId());
                            imgDto.setImageUrl(image.getImageUrl());
                            imgDto.setIsPrimary(image.getIsPrimary());
                            return imgDto;
                        }).toList()
                );
            }

            return dto;
        }).toList();

        SalePostPageResponse response = new SalePostPageResponse();
        response.setContent(content);
        response.setPageNumber(salePostPage.getNumber() + 1); // client thấy page bắt đầu từ 1
        response.setTotalElements(salePostPage.getTotalElements());
        response.setTotalPages(salePostPage.getTotalPages());

        return response;
    }


    /** Lấy post theo id (chỉ ACTIVE) */
    public SalePostResponse getSalePostById(Integer id) {
        SalePost salePost = salePostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        if (salePost.getStatus() != Status.ACTIVE) {
            throw new AppException(ErrorCode.POST_NOT_EXISTED);
        }

        // Set tay DTO
        SalePostResponse dto = new SalePostResponse();
        dto.setId(salePost.getId());
        dto.setTitle(salePost.getTitle());
        dto.setDescription(salePost.getDescription());
        dto.setPrice(salePost.getPrice());
        dto.setQuantity(salePost.getQuantity());
        dto.setLocation(salePost.getLocation());
        dto.setCommissionRate(salePost.getCommissionRate());
        dto.setStatus(salePost.getStatus());
        dto.setCreatedAt(salePost.getCreatedAt());
        dto.setUpdatedAt(salePost.getUpdatedAt());

        if (salePost.getCategory() != null) {
            dto.setCategoryId(salePost.getCategory().getId());
        }

        if (salePost.getAuthor() != null) {
            dto.setAuthorId(String.valueOf(salePost.getAuthor().getId()));
            dto.setAuthorUsername(salePost.getAuthor().getUsername());
            dto.setAuthorAvatar(salePost.getAuthor().getAvatar());
        }

        if (salePost.getImages() != null) {
            dto.setImages(
                    salePost.getImages().stream().map(image -> {
                        SaleImageResponse imgDto = new SaleImageResponse();
                        imgDto.setId(image.getId());
                        imgDto.setImageUrl(image.getImageUrl());
                        imgDto.setIsPrimary(image.getIsPrimary());
                        return imgDto;
                    }).toList()
            );
        }

        return dto;
    }


    /** Lấy post theo category id */
    public SalePostForCategoryPageResponse getSalePostsByCategory(Integer categoryId, int page, int size) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<SalePost> salePostPage = salePostRepository.findByStatusAndCategory_Id(Status.ACTIVE, categoryId, pageable);

        List<SalePostForCategoryResponse> content = salePostMapper.toSalePostForCategoryResponses(salePostPage.getContent());

        SalePostForCategoryPageResponse response = new SalePostForCategoryPageResponse();
        response.setContent(content);
        response.setPageNumber(salePostPage.getNumber() + 1);
        response.setTotalPages(salePostPage.getTotalPages());
        response.setTotalElements(salePostPage.getTotalElements());
        return response;
    }

    /** Tạo bài đăng mới */
    public SalePostResponse createSalePost(String username, SalePostCreateRequest request) {

        // Set Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));


        // Set Author
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        // Backend tự quản lý
        SalePost salePost = new SalePost();
        salePost.setTitle(request.getTitle());
        salePost.setDescription(request.getDescription());
        salePost.setPrice(request.getPrice());
        salePost.setQuantity(request.getQuantity());
        salePost.setLocation(request.getLocation());
        salePost.setCommissionRate(request.getCommissionRate());
        salePost.setCategory(category);
        salePost.setAuthor(author);
        salePost.setStatus(Status.ACTIVE);
        salePost.setCreatedAt(LocalDateTime.now());
        salePost.setUpdatedAt(LocalDateTime.now());

        salePostRepository.save(salePost);

        SalePostResponse salePostResponse = new SalePostResponse();
        salePostResponse.setId(salePost.getId());
        salePostResponse.setTitle(request.getTitle());
        salePostResponse.setDescription(request.getDescription());
        salePostResponse.setPrice(request.getPrice());
        salePostResponse.setQuantity(request.getQuantity());
        salePostResponse.setLocation(request.getLocation());
        salePostResponse.setCommissionRate(request.getCommissionRate());
        salePostResponse.setCategoryId(salePost.getCategory().getId());
        salePostResponse.setAuthorId(salePost.getAuthor().getId());
        salePostResponse.setAuthorUsername(salePost.getAuthor().getUsername());
        salePostResponse.setAuthorAvatar(salePost.getAuthor().getAvatar());
        salePostResponse.setStatus(Status.ACTIVE);
        salePostResponse.setCreatedAt(LocalDateTime.now());
        salePostResponse.setUpdatedAt(LocalDateTime.now());

        notificationService.sendNewSalePost(salePostResponse);
        return  salePostResponse;
    }


    /** Update bài đăng */
    public SalePostResponse updateSalePost(Integer id, SalePostUpdateRequest request) {
        SalePost salePost = salePostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Map field từ request -> entity
        salePostMapper.updateSalePostFromRequest(request, salePost);

        // Set category nếu có
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            salePost.setCategory(category);
        }

        // Backend quản lý updatedAt
        salePost.setUpdatedAt(LocalDateTime.now());

        salePostRepository.save(salePost);

        SalePostResponse salePostResponse = new SalePostResponse();
        salePostResponse.setTitle(salePost.getTitle());
        salePostResponse.setDescription(salePost.getDescription());
        salePostResponse.setPrice(salePost.getPrice());
        salePostResponse.setQuantity(salePost.getQuantity());
        salePostResponse.setLocation(salePost.getLocation());
        salePostResponse.setCommissionRate(salePost.getCommissionRate());
        salePostResponse.setCategoryId(salePost.getCategory().getId());
        salePostResponse.setAuthorId(salePost.getAuthor().getId());
        salePostResponse.setAuthorUsername(salePost.getAuthor().getUsername());
        salePostResponse.setAuthorAvatar(salePost.getAuthor().getAvatar());
        salePostResponse.setStatus(Status.ACTIVE);
        salePostResponse.setCreatedAt(LocalDateTime.now());
        salePostResponse.setUpdatedAt(LocalDateTime.now());

        notificationService.sendUpdatedSalePost(salePostResponse);

        return salePostResponse;
    }



    /** Xóa bài đăng */
    public boolean deleteSalePost(Integer id) {
        boolean result = false;

        SalePost salePost = salePostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        salePost.setStatus(Status.INACTIVE);
        salePostRepository.save(salePost);
        result = true;
        notificationService.sendDeletedSalePost(id);
        return result;
    }
}
