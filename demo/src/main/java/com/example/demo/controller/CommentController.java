package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.CommentRequest;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.response.CommentDTO;
import com.example.demo.dto.response.CommentResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class CommentController {
    CommentService commentService;
    // =============== CREATE ===============
    @PostMapping
    ApiResponse<CommentDTO> createComment(@RequestParam String username, @RequestParam int postId, @RequestBody CommentRequest request) {
        ApiResponse<CommentDTO> response = new ApiResponse<>();
        response.setResult(commentService.createComment(username, postId, request));
        return response;
    }


    // =============== GET ALL COMMENTS OF A POST ===============
    @GetMapping("/post/{postId}")
    public ResponseEntity getCommentsByPost(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId, page, size));
    }

    // =============== GET COMMENT BY ID ===============
    @GetMapping("/{id}")
    public ResponseEntity getCommentById(@PathVariable Integer id) {
        CommentResponse response = commentService.getCommentById(id);
        return ResponseEntity.ok(response);
    }

}
