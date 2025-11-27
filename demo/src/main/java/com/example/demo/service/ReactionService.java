package com.example.demo.service;

import com.example.demo.dto.response.NotificationFCM;
import com.example.demo.dto.response.ReactionPageResponse;
import com.example.demo.dto.response.ReactionResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
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

    @Autowired
    NotificationService notificationService;

    // User like 1 post
    public Reaction likePost(String username, Integer postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        Reaction reaction = reactionRepository.findByPostAndUser(post, user)
                .map(existingReaction -> {
                    if (existingReaction.getStatus() != Status.ACTIVE) {
                        existingReaction.setStatus(Status.ACTIVE);
                        existingReaction.setCreatedAt(LocalDateTime.now());
                        return reactionRepository.save(existingReaction);
                    }
                    return existingReaction;
                })
                .orElseGet(() -> {
                    Reaction like = new Reaction();
                    like.setPost(post);
                    like.setUser(user);
                    like.setStatus(Status.ACTIVE);
                    like.setType("LIKE");
                    like.setCreatedAt(LocalDateTime.now());
                    return reactionRepository.save(like);
                });

        // Broadcast reaction mới
        ReactionResponse dto = ReactionResponse.builder()
                .id(reaction.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .type(reaction.getType())
                .createdAt(reaction.getCreatedAt())
                .build();

        notificationService.broadcastNewReaction(postId, dto);
        // Lấy token của chủ bài post
        String postOwnerToken = post.getAuthor().getFcmToken();

        // Không gửi cho chính người like
        if (postOwnerToken != null && !post.getAuthor().getId().equals(user.getId())) {
            NotificationFCM noti = new NotificationFCM();
            noti.setTitle("Bài viết của bạn có lượt thích mới!");
            noti.setMessage(user.getUsername() + " đã thích bài viết của bạn.");
            noti.setFcmToken(postOwnerToken);

            try {
                notificationService.sendNotification(noti);
            } catch (Exception e) {
                log.error("Lỗi gửi FCM notification", e);
            }
        }

        return reaction;
    }

    // User bỏ like (soft delete)
    public boolean unlikePost(String username, Integer postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        return reactionRepository.findByPostAndUser(post, user).map(reaction -> {
            reaction.setStatus(Status.INACTIVE);
            Reaction saved = reactionRepository.save(reaction);

            // Broadcast reaction bị xóa
            ReactionResponse dto = ReactionResponse.builder()
                    .id(saved.getId())
                    .userId(user.getId())
                    .username(user.getUsername())
                    .avatar(user.getAvatar())
                    .type(saved.getType())
                    .createdAt(saved.getCreatedAt())
                    .build();

            notificationService.broadcastDeletedReaction(postId, dto);
            return true;
        }).orElse(false);
    }

    // Lấy danh sách reaction (chỉ ACTIVE)
    public ReactionPageResponse getReactionsByPost(Integer postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<Reaction> reactionPage = reactionRepository.findByPost_IdAndStatus(postId, Status.ACTIVE, pageable);

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

    // Đếm số lượng like (chỉ ACTIVE)
    public Long countLikes(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return reactionRepository.countByPostAndStatus(post, Status.ACTIVE);
    }

    // Kiểm tra user có like post chưa (chỉ tính ACTIVE)
    public boolean hasUserLiked(String username, Integer postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        return reactionRepository.findByPostAndUser(post, user)
                .filter(r -> r.getStatus() == Status.ACTIVE)
                .isPresent();
    }
}

