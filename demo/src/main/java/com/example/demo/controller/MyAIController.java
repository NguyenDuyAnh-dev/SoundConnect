package com.example.demo.controller;

import com.example.demo.dto.request.PromptRequest;
import com.example.demo.service.AIService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/myai")
@RequiredArgsConstructor
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class MyAIController {
    private final AIService aiService;

    // GET: /api/myai/generate?prompt=Hello Gemini
    @GetMapping("/generate")
    public Map<String, String> generate(@RequestParam String prompt) {
        String result = aiService.callGeminiAPI(prompt);
        return Map.of("response", result);
    }

    // POST: /api/myai/generate
    @PostMapping("/generate")
    public Map<String, String> generatePost(@RequestBody PromptRequest request) {
        String prompt = request.getPrompt();
        String result = aiService.callGeminiAPI(prompt);
        return Map.of("response", result);
    }

}