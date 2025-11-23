package com.example.demo.service;

import com.example.demo.dto.response.PaymentResponse;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PaymentTransactionRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private final PayOS payOS;
    private final UserRepository userRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @PostConstruct
    public void init() {
        log.info("--- PayOS Configuration Check ---");
        log.info("Server URL loaded: {}", serverUrl);
        log.info("Checksum Key loaded: {}", checksumKey != null && !checksumKey.isBlank() ? "OK" : "FAILED - IS NULL OR EMPTY");
        if (!StringUtils.hasText(checksumKey)) {
            throw new IllegalStateException("PayOS checksum-key is not configured. Please check your application.yaml file.");
        }
        log.info("---------------------------------");
    }


    @Transactional
    public PaymentResponse createDepositLink(String userId, BigDecimal amount) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        long orderCode = System.currentTimeMillis();
        String description = "Nap " + amount.intValue() ;

        String returnUrl = serverUrl + "/api/deposits/success";
        String cancelUrl = serverUrl + "/api/deposits/cancel";

        ItemData item = ItemData.builder()
                .name("Nạp tiền vào tài khoản")
                .quantity(1)
                .price(amount.intValue())
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount.intValue())
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CheckoutResponseData data = payOS.createPaymentLink(paymentData);

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

    @Transactional
    public void handleWebhook(String webhookBody) throws Exception {
        if (!StringUtils.hasText(webhookBody)) {
            log.info("Webhook verification request received with empty body. Acknowledging.");
            return;
        }

        Map<String, Object> webhookData = objectMapper.readValue(webhookBody, new TypeReference<>() {});

        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        if (data == null) {
            log.info("Webhook verification request received. No 'data' field found. Acknowledging.");
            return;
        }

        String signature = (String) webhookData.get("signature");
        if (signature == null || !verifySignature(data, signature)) {
            log.error("Webhook verification failed for payment event!");
            throw new SecurityException("Invalid PayOS webhook signature");
        }

        String orderCode = String.valueOf(data.get("orderCode"));

        Optional<PaymentTransaction> transactionOpt = transactionRepository.findByOrderCode(orderCode);

        if (transactionOpt.isEmpty()) {
            log.warn("Transaction not found for order code: {}. This might be a test/verification webhook. Acknowledging.", orderCode);
            return;
        }

        PaymentTransaction transaction = transactionOpt.get();

        if (transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS) {
            log.warn("Transaction {} already processed.", orderCode);
            return;
        }

        PaymentLinkData paymentLinkData = payOS.getPaymentLinkInformation(Long.parseLong(orderCode));
        if ("PAID".equals(paymentLinkData.getStatus())) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);

            User user = transaction.getUser();
            BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
            user.setBalance(currentBalance.add(transaction.getAmount()));

            userRepository.save(user);
            transactionRepository.save(transaction);
            log.info("Successfully processed deposit for order: {}. User {} balance updated.", orderCode, user.getUsername());
        } else {
            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Webhook received for order {} but status is not PAID. Status from PayOS: {}", orderCode, paymentLinkData.getStatus());
        }
    }

    private boolean verifySignature(Map<String, Object> data, String receivedSignature) throws Exception {
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);
        StringBuilder dataToSign = new StringBuilder();
        for (String key : sortedKeys) {
            Object value = data.get(key);

            if (!dataToSign.isEmpty()) {
                dataToSign.append("&");
            }

            // SỬA LỖI: Xử lý null thành chuỗi rỗng thay vì bỏ qua
            String valueAsString = ""; // Mặc định là chuỗi rỗng
            if (value != null) {
                if (value instanceof Map || value instanceof List) {
                    valueAsString = objectMapper.writeValueAsString(value);
                } else {
                    valueAsString = value.toString();
                }
            }
            dataToSign.append(key).append("=").append(valueAsString);
        }

        log.info("Data to sign: {}", dataToSign.toString());

        String expectedSignature = createHmac(dataToSign.toString(), this.checksumKey);

        log.info("Received Signature:  {}", receivedSignature);
        log.info("Expected Signature: {}", expectedSignature);

        return expectedSignature.equals(receivedSignature);
    }

    private String createHmac(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        // ... (phần code này không thay đổi)
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}

