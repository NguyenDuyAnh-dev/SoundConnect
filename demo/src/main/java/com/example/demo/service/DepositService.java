package com.example.demo.service;

import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PaymentTransactionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.*;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private final PayOS payOS;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository transactionRepository;

    @Value("${server.url}")
    private String serverUrl;

    /**
     * TẠO LINK NẠP TIỀN
     */
    @Transactional
    public PaymentResponse createDepositLink(String userId, BigDecimal amount) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Tạo mã đơn hàng ngẫu nhiên (dùng System.currentTimeMillis cho an toàn)
        long orderCode = System.currentTimeMillis() * 1000 + new Random().nextInt(1000);
        // Lưu ý: PayOS yêu cầu orderCode phải là số, và duy nhất.

        String description = "Nap tien " + orderCode;
        String returnUrl = serverUrl + "/api/deposits/success";
        String cancelUrl = serverUrl + "/api/deposits/cancel";

        // 2. Tạo Item (Sản phẩm) - Theo SDK mới
        // Lưu ý: amount của PayOS là Long (số nguyên), cần ép kiểu từ BigDecimal
        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Nạp số dư tài khoản")
                .quantity(1)
                .price(amount.longValue()) // Chuyển BigDecimal sang long
                .build();

        // 3. Tạo Request tạo link
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount.longValue())
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        // 4. Gọi PayOS tạo link (Dùng paymentRequests().create)
        CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

        // 5. Lưu giao dịch PENDING vào DB
        PaymentTransaction transaction = PaymentTransaction.builder()
                .user(user)
                .orderCode(String.valueOf(orderCode))
                .amount(amount)
                .description(description)
                .createdAt(LocalDateTime.now())
                .checkoutUrl(data.getCheckoutUrl())
                .status(PaymentTransaction.TransactionStatus.PENDING)
                .build();
        transactionRepository.save(transaction);

        return new PaymentResponse(data.getCheckoutUrl());
    }

    /**
     * XỬ LÝ WEBHOOK (TỰ ĐỘNG CỘNG TIỀN)
     */
    @Transactional
    public void handleWebhook(String webhookBody) throws Exception {
        log.info("Received PayOS Webhook body: {}", webhookBody);

        WebhookData webhookData;
        try {
            // --- QUAN TRỌNG: SDK TỰ XÁC THỰC CHỮ KÝ Ở ĐÂY ---
            // Nếu chữ ký sai, hàm này sẽ ném Exception ngay lập tức.
            // Bạn không cần hàm verifySignature thủ công nữa.
            webhookData = payOS.webhooks().verify(webhookBody);
        } catch (Exception e) {
            log.error("Webhook verification failed!", e);
            throw new SecurityException("Invalid PayOS webhook signature: " + e.getMessage());
        }

        // Kiểm tra mã lỗi từ PayOS (00 là thành công)
        if (!"00".equals(webhookData.getCode())) {
            log.info("Ignoring webhook event: code={}", webhookData.getCode());
            return;
        }

        long orderCodeLong = webhookData.getOrderCode();
        String orderCode = String.valueOf(orderCodeLong);

        PaymentTransaction transaction = transactionRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND)); // Nhớ thêm error code này hoặc dùng EntityNotFound

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            log.warn("Transaction {} already processed.", orderCode);
            return;
        }

        try {
            // Gọi lại API của PayOS để kiểm tra trạng thái chắc chắn 1 lần nữa (Best Practice)
            PaymentLink paymentLinkData = payOS.paymentRequests().get(orderCodeLong);
            PaymentLinkStatus statusFromAPI = paymentLinkData.getStatus();

            log.info("Checking transaction status for orderCode: {}. Status from PayOS API: {}", orderCode, statusFromAPI);

            if (statusFromAPI == PaymentLinkStatus.PAID) {
                // 1. Cập nhật trạng thái giao dịch
                transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                transactionRepository.save(transaction);

                // 2. CỘNG TIỀN VÀO TÀI KHOẢN USER (Logic gốc của bạn)
                User user = transaction.getUser();
                BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
                user.setBalance(currentBalance.add(transaction.getAmount()));
                userRepository.save(user);

                log.info("Successfully processed deposit for order: {}. User {} balance updated.", orderCode, user.getUsername());
            } else {
                // Xử lý khi thất bại hoặc hủy
                transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);
                log.error("Webhook received but status is NOT 'PAID'. Actual status: '{}'", statusFromAPI);
            }
        } catch (Exception e) {
            log.error("Could not confirm payment with PayOS API for order {}. Error: {}", orderCode, e.getMessage());
            throw e;
        }
    }
}