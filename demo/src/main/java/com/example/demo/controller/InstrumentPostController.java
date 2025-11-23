package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.InstrumentPostRequest;
import com.example.demo.dto.response.InstrumentPostResponse;
import com.example.demo.service.InstrumentPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postsinstrument")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InstrumentPostController {

      InstrumentPostService postService;

    @ResponseStatus(HttpStatus.CREATED) // Thêm annotation để trả về mã 201
    @PostMapping
    public ApiResponse<InstrumentPostResponse> createPost(@Valid @RequestBody InstrumentPostRequest request) {
        InstrumentPostResponse createdPost = postService.createPost(request);
        // Đóng gói kết quả vào ApiResponse chuẩn và trả về trực tiếp
        return ApiResponse.<InstrumentPostResponse>builder()
                .result(createdPost)
                .message("Post created successfully and fee deducted.") // Thêm message mô tả
                .build();
    }

    // API để cập nhật bài đăng đã có
    @PutMapping("/{postId}") // Dùng PUT cho việc cập nhật toàn bộ hoặc một phần
    public ApiResponse<InstrumentPostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody InstrumentPostRequest request) {
        // Lấy username đã được chuyển vào service
        InstrumentPostResponse updatedPost = postService.updatePost(postId, request); // Bỏ currentUsername

        // Đóng gói kết quả và trả về trực tiếp (mặc định 200 OK)
        return ApiResponse.<InstrumentPostResponse>builder()
                .result(updatedPost)
                .message("Post updated successfully.")
                .build();
    }


    // API để xóa (gỡ) bài đăng
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.OK) // Trả về 200 OK (hoặc No Content tùy chọn)
    public ApiResponse<Void> deletePost(@PathVariable Long postId) { // Trả về ApiResponse<Void> hoặc ApiResponse<String>
        // Lấy username đã được chuyển vào service
        postService.deletePost(postId); // Bỏ currentUsername

        // Trả về ApiResponse thành công
        return ApiResponse.<Void>builder()
                .message("Post deleted successfully.")
                .build();
    }

    // API để lấy danh sách tất cả bài đăng
    @GetMapping
    public ApiResponse<List<InstrumentPostResponse>> getAllPosts() {
        // Gọi service để lấy danh sách
        List<InstrumentPostResponse> posts = postService.getAllPosts();

        // Đóng gói kết quả và trả về trực tiếp (mặc định 200 OK)
        return ApiResponse.<List<InstrumentPostResponse>>builder()
                .result(posts)
                .build();
    }
}

