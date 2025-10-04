package com.example.demo.controller;

import com.example.demo.dto.response.SaleImageResponse;
import com.example.demo.entity.SaleImage;
import com.example.demo.service.SaleImageService;
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
@RequestMapping("/saleImage")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class SaleImageController {
    @Autowired
    SaleImageService saleImageService;

    @PostMapping(value = "/{postId}/images", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<SaleImageResponse> uploadImage(
            @PathVariable Integer postId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPrimary
    ) throws IOException {
        return ResponseEntity.ok(saleImageService.uploadImageToPost(postId, file, isPrimary));
    }

    @GetMapping("/{salePostId}")
    public ResponseEntity getImagesByPost(@PathVariable Integer salePostId) {
        return ResponseEntity.ok(saleImageService.getImagesBySalePost(salePostId));
    }

    @PutMapping("/set-primary/{imageId}")
    public ResponseEntity<SaleImageResponse> setPrimary(@PathVariable Integer imageId) {
        return ResponseEntity.ok(saleImageService.setPrimaryImage(imageId));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Boolean> deleteImage(@PathVariable Integer imageId) {
        return ResponseEntity.ok(saleImageService.deleteSaleImage(imageId));
    }
}
