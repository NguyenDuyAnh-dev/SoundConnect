package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDepositRequest {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "1000", message = "Minimum deposit amount is 1000")
    private BigDecimal amount; // Số tiền người dùng muốn nạp
}
