package com.example.demo.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/myai")
@RequiredArgsConstructor
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class MyAiController {
    private final AIService aiService;

    // Model cho request
    @Getter
    public static class ChatRequest {
        private String context;

        public void setContext(String context) {
            this.context = context;
        }
    }

    // Model cho response
    public static class ChatResponse {
        private String reply;
        public ChatResponse(String reply) {
            this.reply = reply;
        }
        public String getReply() {
            return reply;
        }
    }

    @PostMapping("/chat")
    public ChatResponse askAI(@RequestBody ChatRequest request) {
        String reply = aiService.askAI(request.getContext());
        return new ChatResponse(reply);
    }

    @PostMapping("/flashcard")
    public ChatResponse generateFlashcard() {
        String quote = aiService.generateFlashcard();
        return new ChatResponse(quote);
    }
}
