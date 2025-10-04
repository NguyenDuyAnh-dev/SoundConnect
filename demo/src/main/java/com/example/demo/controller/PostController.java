package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.PostRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostPageResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class PostController {
    PostService postService;

    //  Tạo post mới
    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<PostResponse> createPost(
            @RequestParam String username,
            @ModelAttribute PostRequest postRequest) throws IOException {
        PostResponse response = postService.createPost(username, postRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{postId}", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Integer postId,      // Lấy từ URL
            @RequestParam String username,     // Hoặc lấy từ token
            @ModelAttribute PostUpdateRequest postRequest) throws IOException {

        return ResponseEntity.ok(postService.updatePost(username, postId, postRequest));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity deletePost(
            @PathVariable Integer postId,
            @RequestParam String username) {

        boolean result = postService.deletePost(username, postId);
        String resultString = "";
        if(result){
            resultString = "Deleted successfully";
        }else{
            resultString= "Deleted Error";
        }
        return ResponseEntity.ok(resultString);
    }



    //  Lấy tất cả post của một user
    @GetMapping("/posts/users/{username}")
    public ResponseEntity getPostsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPostsByUser(username, page, size));
    }

    //  Tìm post theo từ khóa trong caption
    @GetMapping("/search")
    public ResponseEntity searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostPageResponse> result = postService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    // API lấy toàn bộ bài viết (có phân trang, sort)
    @GetMapping
    public ResponseEntity<Page<PostPageResponse>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PostPageResponse> response = postService.getAllPosts(page, size);
        return ResponseEntity.ok(response);
    }


    //  Lấy post theo id
    @GetMapping("/{id}")
    public ApiResponse<Post> getPostById(@PathVariable Integer id) {
        Post post = postService.getPostById(id);

        return ApiResponse.<Post>builder()
                .result(post)
                .build();
    }
}
