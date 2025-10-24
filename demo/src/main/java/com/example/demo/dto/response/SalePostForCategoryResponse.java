package com.example.demo.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalePostForCategoryResponse {
    private Integer id;
    private String title;
    private String description;
    private Double price;
    private LocalDateTime createdAt;
}
