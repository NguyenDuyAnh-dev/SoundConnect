package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${spring.ai.gemini.api-key}")
    private String API_KEY;

    @Value("${spring.ai.gemini.model}")
    private String MODEL;


    public String callGeminiAPI(String prompt) {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + API_KEY);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // --- ĐÃ SỬA LỖI JSON PAYLOAD ---
            // Escape các ký tự đặc biệt trong prompt để tạo ra một chuỗi JSON hợp lệ.
            String jsonEscapedPrompt = prompt
                    .replace("\\", "\\\\") // Escape backslashes first
                    .replace("\"", "\\\"")  // Escape double quotes
                    .replace("\n", "\\n");   // Escape newlines

            String body = """
            {
              "contents": [{"parts": [{"text": "%s"}]}]
            }
            """.formatted(jsonEscapedPrompt); // Sử dụng chuỗi đã được escape

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            InputStreamReader isr;
            if (responseCode >= 400) {
                isr = new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8);
            } else {
                isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
            }

            String response;
            try (BufferedReader reader = new BufferedReader(isr)) {
                response = reader.lines().collect(Collectors.joining("\n"));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            if (responseCode >= 400) {
                JsonNode errorMessage = root.at("/error/message");
                String error = errorMessage.isMissingNode() ? response : errorMessage.asText();
                log.error("Lỗi từ Gemini API ({}): {}", responseCode, error);
                return "Lỗi từ API: " + error;
            }

            JsonNode candidateText = root.at("/candidates/0/content/parts/0/text");
            if (!candidateText.isMissingNode()) {
                return candidateText.asText();
            } else {
                JsonNode finishReason = root.at("/candidates/0/finishReason");
                if (!finishReason.isMissingNode()) {
                    log.warn("Phản hồi bị chặn, lý do: {}", finishReason.asText());
                    return "Phản hồi bị chặn: " + finishReason.asText();
                }
                log.warn("Không có nội dung văn bản trong phản hồi từ AI: {}", response);
                return "Không có phản hồi từ AI";
            }

        } catch (IOException e) {
            log.error("Lỗi IO khi gọi Gemini API", e);
            return "Lỗi khi gọi Gemini API: " + e.getMessage();
        }
    }

    public boolean isMusicRelated(String content) {
        if (content == null || content.isBlank()) {
            return false; // nếu rỗng hoặc null coi như không liên quan
        }

        // Escape đủ tất cả ký tự đặc biệt để JSON hợp lệ
        String jsonEscapedContent = content
                .replace("\\", "\\\\")   // escape backslash
                .replace("\"", "\\\"")   // escape double quote
                .replace("\n", "\\n")    // escape newline
                .replace("\r", "\\r");   // escape carriage return

        String prompt = """
        Bạn là hệ thống kiểm duyệt nội dung mạng xã hội về âm nhạc.
        Hãy trả lời chính xác 'true' hoặc 'false', không giải thích thêm.

        --- Ví dụ ---
        Nội dung: "Mình muốn bán cây đàn guitar này." -> true
        Nội dung: "Thời tiết hôm nay đẹp quá." -> false
        Nội dung: "string" -> false
        Nội dung: "Bài hát mới của Sơn Tùng M-TP" -> true
        --- Hết ví dụ ---

        Bây giờ phân loại nội dung sau:
        Nội dung: "%s" ->
    """.formatted(jsonEscapedContent);

        String result = callGeminiAPI(prompt).trim().toLowerCase();

        // chỉ trả true khi Gemini thực sự trả 'true'
        return result.startsWith("true");
    }

}