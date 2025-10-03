package com.example.demo.dto.request;

import com.example.demo.enums.Status;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    private String name;
    private String description;
    private Status status;
}
