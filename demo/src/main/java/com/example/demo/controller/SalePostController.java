package com.example.demo.controller;

import com.example.demo.entity.SalePost;
import com.example.demo.service.SalePostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity getAllSalePosts() {
        return ResponseEntity.ok(salePostService.getAllSalePosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity getSalePostById(@PathVariable Integer id) {
        return salePostService.getSalePostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity createSalePost(@RequestBody SalePost salePost) {
        return ResponseEntity.ok(salePostService.createSalePost(salePost));
    }

    @PutMapping("/{id}")
    public ResponseEntity updateSalePost(@PathVariable Integer id, @RequestBody SalePost salePost) {
        return ResponseEntity.ok(salePostService.updateSalePost(id, salePost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSalePost(@PathVariable Integer id) {
        salePostService.deleteSalePost(id);
        return ResponseEntity.noContent().build();
    }
}
