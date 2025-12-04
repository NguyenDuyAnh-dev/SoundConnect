package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.InstrumentPostRequest;
import com.example.demo.dto.response.InstrumentPostResponse;
import com.example.demo.service.InstrumentPostService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/postsinstrument")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "api")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InstrumentPostController {

    InstrumentPostService postService;

    /**
     * TẠO BÀI ĐĂNG MỚI
     * Sử dụng @ModelAttribute để map các trường từ Form-Data vào DTO tự động.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InstrumentPostResponse> createPost(
            // @ModelAttribute: Tự động lấy title, price, description từ form
            // @Valid: Kiểm tra validation trong DTO (Not blank, DecimalMin...)
            @ParameterObject
            @Valid @ModelAttribute InstrumentPostRequest request,

            // Nhận file ảnh riêng lẻ
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        log.info("Dữ liệu nhận được: Title={}, Price={}", request.getTitle(), request.getPrice());
        // Gọi service xử lý logic + upload ảnh
        InstrumentPostResponse createdPost = postService.createPost(request, image);

        return ApiResponse.<InstrumentPostResponse>builder()
                .result(createdPost)
                .message("Post created successfully and fee deducted.")
                .build();
    }

    /**
     * CẬP NHẬT BÀI ĐĂNG
     * Cũng dùng @ModelAttribute để tiện cho việc gửi cả text lẫn file mới (nếu có)
     */
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InstrumentPostResponse> updatePost(
            @PathVariable Long postId,
            @ParameterObject
            @Valid @ModelAttribute InstrumentPostRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        InstrumentPostResponse updatedPost = postService.updatePost(postId, request, image);

        return ApiResponse.<InstrumentPostResponse>builder()
                .result(updatedPost)
                .message("Post updated successfully.")
                .build();
    }

    /**
     * XÓA BÀI ĐĂNG
     */
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);

        return ApiResponse.<Void>builder()
                .message("Post deleted successfully.")
                .build();
    }

    /**
     * LẤY DANH SÁCH BÀI ĐĂNG
     */
    @GetMapping
    public ApiResponse<List<InstrumentPostResponse>> getAllPosts() {
        List<InstrumentPostResponse> posts = postService.getAllPosts();

        return ApiResponse.<List<InstrumentPostResponse>>builder()
                .result(posts)
                .build();
    }
    // --- MỚI THÊM: LẤY CHI TIẾT 1 BÀI ĐĂNG ---
    @GetMapping("/{postId}")
    public ApiResponse<InstrumentPostResponse> getPostById(@PathVariable Long postId) {
        InstrumentPostResponse post = postService.getPostById(postId);

        return ApiResponse.<InstrumentPostResponse>builder()
                .result(post)
                .build();
    }
}