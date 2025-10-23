package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.enums.Status;
import com.example.demo.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "api")
public class AdminController {
    AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.<Map<String, Object>>builder()
                .result(adminService.getDashboardStats())
                .build();
    }
}


