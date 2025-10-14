package com.example.demo.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SalePostPageResponse {
    private List<SalePostResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
