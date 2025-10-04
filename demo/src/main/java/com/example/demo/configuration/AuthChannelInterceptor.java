package com.example.demo.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    // SỬA LỖI: Chuyển sang Field Injection để phá vỡ vòng lặp phụ thuộc
    @Autowired @Lazy
    private CustomJwtDecoder customJwtDecoder;

    @Autowired @Lazy
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header for WebSocket: {}", authHeader);

            if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header for WebSocket connection.");
                throw new AccessDeniedException("Unauthorized: Missing Bearer token.");
            }

            String token = authHeader.substring(7);
            try {
                // Dùng CustomJwtDecoder để giải mã và xác thực token
                Jwt jwt = customJwtDecoder.decode(token);

                // Dùng converter đã được inject để chuyển đổi JWT thành đối tượng Authentication
                Authentication authentication = jwtAuthenticationConverter.convert(jwt);

                // Gắn user đã được xác thực vào session WebSocket
                accessor.setUser(authentication);
                log.info("Authenticated user '{}' for WebSocket session.", authentication.getName());

            } catch (Exception e) {
                // Ném exception để TỪ CHỐI kết nối nếu token không hợp lệ
                log.error("WebSocket Authentication error: {}", e.getMessage());
                throw new AccessDeniedException("Unauthorized: Invalid token.");
            }
        }
        return message;
    }
}

