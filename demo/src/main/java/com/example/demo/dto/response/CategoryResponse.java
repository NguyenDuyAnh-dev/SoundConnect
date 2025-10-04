package com.example.demo.dto.response;

import com.example.demo.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private Status status;
    private LocalDateTime createdAt;
}
