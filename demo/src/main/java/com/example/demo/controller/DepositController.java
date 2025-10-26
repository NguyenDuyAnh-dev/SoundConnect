package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.CreateDepositRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.service.DepositService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class DepositController {

    private final DepositService depositService;

    @Value("${client.url}")
    private String clientUrl;

    @PostMapping("/create-link")
    public ResponseEntity<ApiResponse<PaymentResponse>> createDepositLink(@Valid @RequestBody CreateDepositRequest request) throws Exception {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        PaymentResponse paymentResponse = depositService.createDepositLink(currentUserId, request.getAmount());

        ApiResponse<PaymentResponse> response = ApiResponse.<PaymentResponse>builder()
                .result(paymentResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public void handleSuccessRedirect(HttpServletResponse response, @RequestParam String orderCode) throws IOException {
        // Chỉ chuyển hướng về frontend, không xử lý logic ở đây
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-success")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/cancel")
    public void handleCancelRedirect(HttpServletResponse response, @RequestParam String orderCode) throws IOException {
        // Chỉ chuyển hướng về frontend, không xử lý logic ở đây
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-cancelled")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<String> payosWebhook(@RequestBody String webhookBody) {
        try {
            depositService.handleWebhook(webhookBody);
            // Luôn trả về 200 OK để PayOS biết đã nhận được webhook
            return ResponseEntity.ok("Webhook received and processed.");
        } catch (SecurityException e) {
            log.error("Webhook security exception: {}", e.getMessage());
            // Trả về lỗi nếu chữ ký không hợp lệ
            return ResponseEntity.status(401).body("Invalid signature.");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            // Trả về lỗi nếu có vấn đề khác trong quá trình xử lý
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
}

