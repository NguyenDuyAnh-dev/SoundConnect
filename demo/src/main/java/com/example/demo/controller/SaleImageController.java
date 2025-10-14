package com.example.demo.controller;

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

    @GetMapping
    public ResponseEntity getAllSaleImages() {
        return ResponseEntity.ok(saleImageService.getAllSaleImages());
    }

    @GetMapping("/{id}")
    public ResponseEntity getSaleImageById(@PathVariable Integer id) {
        return saleImageService.getSaleImageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sale-post/{salePostId}")
    public ResponseEntity getImagesBySalePost(@PathVariable Integer salePostId) {
        return ResponseEntity.ok(saleImageService.getImagesBySalePost(salePostId));
    }

    @GetMapping("/sale-post/{salePostId}/primary")
    public ResponseEntity getPrimaryImage(@PathVariable Integer salePostId) {
        return ResponseEntity.ok(saleImageService.getPrimaryImageBySalePost(salePostId));
    }

    @PostMapping
    public ResponseEntity createSaleImage(@RequestBody SaleImage saleImage) {
        return ResponseEntity.ok(saleImageService.createSaleImage(saleImage));
    }

    @PutMapping("/{id}")
    public ResponseEntity updateSaleImage(@PathVariable Integer id, @RequestBody SaleImage saleImageDetails) {
        return ResponseEntity.ok(saleImageService.updateSaleImage(id, saleImageDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSaleImage(@PathVariable Integer id) {
        saleImageService.deleteSaleImage(id);
        return ResponseEntity.noContent().build();
    }
}
