package com.example.demo.controller;

import com.example.demo.entity.Reaction;
import com.example.demo.service.ReactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reaction")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ReactionController {
    ReactionService reactionService;

    // User like 1 post
    @PostMapping("/like")
    public Reaction likePost(@RequestParam String username,
                             @RequestParam Integer postId) {
        return reactionService.likePost(username, postId);
    }

    // User bỏ like
    @DeleteMapping("/unlike")
    public void unlikePost(@RequestParam String username,
                           @RequestParam Integer postId) {
        reactionService.unlikePost(username, postId);
    }

    // Lấy danh sách reaction (like) của 1 post
    @GetMapping("/list/{postId}")
    public ResponseEntity getReactionsByPost(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reactionService.getReactionsByPost(postId, page, size));
    }

    // Đếm số lượng like
    @GetMapping("/count/{postId}")
    public Long countLikes(@PathVariable Integer postId) {
        return reactionService.countLikes(postId);
    }

    // Kiểm tra user có like post chưa
    @GetMapping("/hasLiked")
    public boolean hasUserLiked(@RequestParam String username,
                                @RequestParam Integer postId) {
        return reactionService.hasUserLiked(username, postId);
    }

}
