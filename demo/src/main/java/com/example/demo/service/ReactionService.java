package com.example.demo.service;

import com.example.demo.dto.response.ReactionPageResponse;
import com.example.demo.dto.response.ReactionResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionService {
    @Autowired
    ReactionRepository reactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    // User like 1 post
    public Reaction likePost(String username, Integer postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Kiểm tra nếu user đã like post rồi thì return luôn reaction cũ
        return reactionRepository.findByPostAndUser(post, user)
                .orElseGet(() -> {
                    Reaction like = new Reaction();
                    like.setPost(post);
                    like.setUser(user);
                    like.setCreatedAt(LocalDateTime.now());
                    return reactionRepository.save(like);
                });
    }

    // User bỏ like
    public void unlikePost(String username, Integer postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        reactionRepository.findByPostAndUser(post, user)
                .ifPresent(reactionRepository::delete);
    }


    // Lấy danh sách like của 1 post
    public ReactionPageResponse getReactionsByPost(Integer postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Reaction> reactionPage = reactionRepository.findByPost_Id(postId, pageable);

        List<ReactionResponse> reactionResponses = reactionPage.getContent().stream()
                .map(r -> ReactionResponse.builder()
                        .id(r.getId())
                        .userId(r.getUser().getId())
                        .username(r.getUser().getUsername())
                        .avatar(r.getUser().getAvatar())
                        .type(r.getType())
                        .createdAt(r.getCreatedAt())
                        .build()
                ).toList();

        return ReactionPageResponse.builder()
                .content(reactionResponses)
                .pageNumber(reactionPage.getNumber())
                .totalElements(reactionPage.getTotalElements())
                .totalPages(reactionPage.getTotalPages())
                .build();
    }


    // Đếm số lượng like
    public Long countLikes(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return reactionRepository.countByPost(post);
    }

    // Kiểm tra user có like post chưa
    public boolean hasUserLiked(String username, Integer postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return reactionRepository.findByPostAndUser(post, user).isPresent();
    }
}
