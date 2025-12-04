package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponsePayment;
import com.example.demo.dto.request.CreateDepositRequest;
import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.service.DepositService;

// Import Swagger (OpenAPI)
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@CrossOrigin("*") // Cho phép Frontend gọi thoải mái
@SecurityRequirement(name = "api") // Yêu cầu xác thực JWT cho toàn bộ Controller (trừ những cái public)
@Tag(name = "Deposit Management", description = "Các API liên quan đến nạp tiền vào tài khoản")
public class DepositController {

    private final DepositService depositService;

    @Value("${client.url}")
    private String clientUrl;

    // --- 1. TẠO LINK NẠP TIỀN ---
    @PostMapping("/create-link")
    @Operation(summary = "Tạo link nạp tiền (Deposit)", description = "Tạo link thanh toán PayOS để user nạp tiền vào số dư. Yêu cầu đăng nhập.")
//    @ApiResponses(value = {
//            @SwaggerApiResponse(responseCode = "200", description = "Tạo link thành công"),
//            @SwaggerApiResponse(responseCode = "500", description = "Lỗi server hoặc lỗi PayOS")
//    })
    public ResponseEntity<ApiResponsePayment<PaymentResponse>> createDepositLink(@Valid @RequestBody CreateDepositRequest request) throws Exception {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Gọi Service (đã update V2)
        PaymentResponse paymentResponse = depositService.createDepositLink(currentUserId, request.getAmount());

        ApiResponsePayment<PaymentResponse> response = ApiResponsePayment.<PaymentResponse>builder()
                .result(paymentResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    // --- 2. CALLBACK THÀNH CÔNG (Redirect về Frontend) ---
    @GetMapping("/success")
    @Hidden // Ẩn khỏi Swagger vì đây là URL trình duyệt redirect, ko phải API cho dev dùng
    public void handleSuccessRedirect(HttpServletResponse response, @RequestParam String orderCode) throws IOException {
        // Chỉ chuyển hướng về frontend, không xử lý logic ở đây (Logic nằm ở Webhook)
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-success")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    // --- 3. CALLBACK HỦY (Redirect về Frontend) ---
    @GetMapping("/cancel")
    @Hidden // Ẩn khỏi Swagger
    public void handleCancelRedirect(HttpServletResponse response, @RequestParam String orderCode) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromHttpUrl(clientUrl + "/payment-cancelled")
                .queryParam("orderCode", orderCode)
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    // --- 4. WEBHOOK (PayOS gọi vào để báo kết quả) ---
    @PostMapping("/payos-webhook")
    @Hidden // Rất quan trọng: Ẩn endpoint này đi để hacker không spam bậy bạ
    public ResponseEntity<String> payosWebhook(@RequestBody String webhookBody) {
        try {
            depositService.handleWebhook(webhookBody);

            // Luôn trả về 200 OK để PayOS biết đã nhận được webhook
            return ResponseEntity.ok("Webhook received and processed.");

        } catch (AppException e) {
            // --- LOGIC MỚI: XỬ LÝ TEST WEBHOOK ---
            // Khi bạn setup Webhook trên PayOS Dashboard, PayOS sẽ gửi 1 request test với orderCode giả.
            // Service sẽ ném lỗi TRANSACTION_NOT_FOUND.
            // Chúng ta phải bắt lỗi này và trả về 200 OK để PayOS xác nhận webhook "Xanh".
            if (e.getErrorCode() == ErrorCode.TRANSACTION_NOT_FOUND) {
                log.info("Webhook verification request detected (Transaction not found). Responding 200 OK for PayOS verification.");
                return ResponseEntity.ok("Webhook verification successful");
            }

            log.error("AppException in Webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook logic failed: " + e.getMessage());

        } catch (SecurityException e) {
            log.error("Webhook security exception: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid signature.");

        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
}