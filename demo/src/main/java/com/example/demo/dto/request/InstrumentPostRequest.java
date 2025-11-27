package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// DTO dùng để nhận dữ liệu khi tạo hoặc cập nhật bài đăng
@Data
@NoArgsConstructor // <--- Bắt buộc phải có để Spring tạo object rỗng trước
@AllArgsConstructor // <--- Thêm cho đầy đủ
public class InstrumentPostRequest {
    private String title;
    private String description;
    private BigDecimal price;
}

