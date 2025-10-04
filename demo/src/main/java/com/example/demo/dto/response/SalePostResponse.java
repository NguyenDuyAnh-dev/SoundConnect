package com.example.demo.dto.response;

import com.example.demo.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalePostResponse {
    private Integer id;
    private String title;
    private String description;
    private Double price;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private User author;
}
