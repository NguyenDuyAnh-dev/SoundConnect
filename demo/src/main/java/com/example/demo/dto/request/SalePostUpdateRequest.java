package com.example.demo.dto.request;

import com.example.demo.enums.Status;
import lombok.Data;

@Data
public class SalePostUpdateRequest {
    private String title;
    private String description;
    private Double price;
    private Integer quantity;
    private String location;
    private Double commissionRate;
    private Status status;
    private Integer categoryId;
}
