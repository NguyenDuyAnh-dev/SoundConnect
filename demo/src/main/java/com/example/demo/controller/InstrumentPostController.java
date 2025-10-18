package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.InstrumentPostRequest;
import com.example.demo.dto.response.InstrumentPostResponse;
import com.example.demo.service.InstrumentPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/postinstrument")
@RequiredArgsConstructor
public class InstrumentPostController {

    private final InstrumentPostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<InstrumentPostResponse>> createPost(@Valid @RequestBody InstrumentPostRequest request) {
        // Lấy ID của người dùng đã được xác thực từ token
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        InstrumentPostResponse createdPost = postService.createPost(request, currentUserId);
        ApiResponse<InstrumentPostResponse> response = ApiResponse.<InstrumentPostResponse>builder()
                .result(createdPost)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        // Lấy ID của người dùng đã được xác thực
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        postService.deletePost(postId, currentUserId);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content khi xóa thành công
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstrumentPostResponse>>> getAllPosts() {
        List<InstrumentPostResponse> posts = postService.getAllPosts();
        ApiResponse<List<InstrumentPostResponse>> response = ApiResponse.<List<InstrumentPostResponse>>builder()
                .result(posts)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<InstrumentPostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody InstrumentPostRequest request) {

        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        InstrumentPostResponse updatedPost = postService.updatePost(postId, request, currentUserId);
        ApiResponse<InstrumentPostResponse> response = ApiResponse.<InstrumentPostResponse>builder()
                .result(updatedPost)
                .build();
        return ResponseEntity.ok(response);
    }
}

