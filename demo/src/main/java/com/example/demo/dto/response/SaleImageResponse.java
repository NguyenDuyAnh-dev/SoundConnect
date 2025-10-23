package com.example.demo.dto.response;

import lombok.Data;

@Data
public class SaleImageResponse {
    private Integer id;
    private String imageUrl;
    private Boolean isPrimary;
}
