package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedFollowerResponse {
    private List<FollowerResponse> followers;
    private long totalElements; // tổng số follower
    private int totalPages;     // tổng số trang
    private int currentPage;    // trang hiện tại
    private int pageSize;       // kích thước trang
}
