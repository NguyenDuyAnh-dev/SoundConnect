package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.CreateDepositRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.service.DepositService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
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
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-success")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/cancel")
    public void handleCancelRedirect(HttpServletResponse response, @RequestParam String orderCode) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-cancelled")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<String> payosWebhook(@RequestBody String webhookBody) {
        try {
            depositService.handleWebhook(webhookBody);
            return ResponseEntity.ok("Webhook received and processed.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
}
