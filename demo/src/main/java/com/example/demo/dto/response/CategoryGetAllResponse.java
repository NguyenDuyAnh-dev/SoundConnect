package com.example.demo.dto.response;

import com.example.demo.enums.Status;
import lombok.Data;

@Data
public class CategoryGetAllResponse {
    private Integer id;
    private String name;
    private String description;
    private Status status;
}
