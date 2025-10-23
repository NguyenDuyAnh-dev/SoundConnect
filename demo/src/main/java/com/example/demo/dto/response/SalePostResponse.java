package com.example.demo.dto.response;

import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalePostResponse {
    private Integer id;
    private String title;
    private String description;
    private Double price;
    private Integer quantity;
    private String location;
    private Double commissionRate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer categoryId;

    private String authorId;
    private String authorUsername;
    private String authorAvatar;

    private List<SaleImageResponse> images;
}
