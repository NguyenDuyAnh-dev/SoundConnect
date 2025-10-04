package com.example.demo.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SalePostForCategoryPageResponse {
    private List<SalePostForCategoryResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
