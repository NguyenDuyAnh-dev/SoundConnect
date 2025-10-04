package com.example.demo.dto.request;

import lombok.Data;

@Data
public class SalePostCreateRequest {
    private String title;
    private String description;
    private Double price;
    private Integer quantity;
    private String location;
    private Double commissionRate;
    private Integer categoryId;   // FE chỉ gửi ID category
    private String username;      // FE chỉ gửi Username
}
