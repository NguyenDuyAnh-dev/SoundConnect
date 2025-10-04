package com.example.demo.controller;

import com.example.demo.dto.request.SalePostCreateRequest;
import com.example.demo.dto.request.SalePostUpdateRequest;
import com.example.demo.dto.response.SaleImageResponse;
import com.example.demo.dto.response.SalePostForCategoryPageResponse;
import com.example.demo.dto.response.SalePostPageResponse;
import com.example.demo.dto.response.SalePostResponse;
import com.example.demo.entity.SalePost;
import com.example.demo.service.SaleImageService;
import com.example.demo.service.SalePostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/salePost")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class SalePostController {
    @Autowired
    SalePostService salePostService;

    @Autowired
    SaleImageService saleImageService;

    /** Lấy tất cả post (ACTIVE) */
    @GetMapping
    public ResponseEntity<SalePostPageResponse> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(salePostService.getAllSalePosts(page, size));
    }

    /** Lấy post theo id */
    @GetMapping("/{id}")
    public ResponseEntity<SalePostResponse> getPostById(@PathVariable Integer id) {
        return ResponseEntity.ok(salePostService.getSalePostById(id));
    }

    /** Lấy post theo category */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<SalePostForCategoryPageResponse> getPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(salePostService.getSalePostsByCategory(categoryId, page, size));
    }

    /** Tạo mới post */
    @PostMapping
    public ResponseEntity createPost(@RequestBody SalePostCreateRequest salePost) {
        return ResponseEntity.ok(salePostService.createSalePost(salePost));
    }

    /** Update post */
    @PutMapping("/{id}")
    public ResponseEntity<SalePostResponse> updatePost(@PathVariable Integer id, @RequestBody SalePostUpdateRequest updatedPost) {
        return ResponseEntity.ok(salePostService.updateSalePost(id, updatedPost));
    }

    /** Xóa post */
    @DeleteMapping("/{id}")
    public ResponseEntity deletePost(@PathVariable Integer id) {
        boolean result = salePostService.deleteSalePost(id);
        String resultString = "";
        if(result){
            resultString = "Deleted successfully";
        }else{
            resultString= "Deleted Error";
        }
        return ResponseEntity.ok(resultString);
    }

    /** Upload ảnh cho post */
    @PostMapping("/{postId}/images")
    public ResponseEntity<SaleImageResponse> uploadImage(
            @PathVariable Integer postId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPrimary
    ) throws IOException {
        return ResponseEntity.ok(saleImageService.uploadImageToPost(postId, file, isPrimary));
    }
}
