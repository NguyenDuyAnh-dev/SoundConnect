package com.example.demo.service;

import com.example.demo.dto.request.CommentRequest;
import com.example.demo.dto.request.CommentUpdateRequest;
import com.example.demo.dto.response.CommentDTO;
import com.example.demo.dto.response.CommentPageResponse;
import com.example.demo.dto.response.CommentResponse;
import com.example.demo.dto.response.NotificationFCM;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
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
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    NotificationService notificationService;


    // Tạo comment mới
    public CommentDTO createComment(String username, int postId, CommentRequest request) {
//        Comment comment = modelMapper.map(request, Comment.class);
        Comment comment = new Comment();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(request.getContent());
        comment.setCommentTime(LocalDateTime.now());
        comment.setStatus(Status.ACTIVE);

        Comment savedComment = commentRepository.save(comment);
        CommentDTO response = new CommentDTO();
        response.setPost(comment.getPost());
        response.setAuthorName(comment.getAuthor().getName());
        response.setContent(comment.getContent());
        response.setCommentTime(comment.getCommentTime());
        response.setAvatar(comment.getAuthor().getAvatar());
        response.setStatus(comment.getStatus());

        // Broadcast realtime comment mới tới tất cả client đang subscribe
        notificationService.broadcastNewComment(postId, response);
        // Push notification cho chủ bài post (nếu không phải tự comment chính bài của mình)
        if (!post.getAuthor().getId().equals(user.getId())) {
            if (post.getAuthor().getFcmToken() != null) {
                NotificationFCM noti = new NotificationFCM();
                noti.setTitle("Bài viết của bạn có bình luận mới");
                noti.setMessage(user.getName() + " đã bình luận: " + comment.getContent());
                noti.setFcmToken(post.getAuthor().getFcmToken());

                notificationService.sendNotification(noti);
            }
        }
        return response;
    }

    // Update comment
    public CommentDTO updateComment(String username, int commentId, CommentUpdateRequest request) {
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tìm comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        // Check quyền: chỉ author mới được update
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Cập nhật dữ liệu
        comment.setContent(request.getContent());
        comment.setCommentTime(LocalDateTime.now()); // update lại thời gian
        comment.setStatus(request.getStatus());

        Comment updatedComment = commentRepository.save(comment);

        // Mapping sang DTO
        CommentDTO response = new CommentDTO();
        response.setPost(updatedComment.getPost());
        response.setAuthorName(updatedComment.getAuthor().getName());
        response.setContent(updatedComment.getContent());
        response.setCommentTime(updatedComment.getCommentTime());
        response.setAvatar(updatedComment.getAuthor().getAvatar());
        response.setStatus(updatedComment.getStatus());

        // Broadcast realtime cập nhật comment
        notificationService.broadcastUpdatedComment(
                updatedComment.getPost().getId(),
                response
        );

        return response;
    }


    // Delete comment (soft delete)
    public boolean deleteComment(String username, int commentId) {
        boolean result;
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tìm comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        // Check quyền: chỉ author mới được xóa
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Xoá mềm -> đổi status thành DELETED
        comment.setStatus(Status.INACTIVE);
        commentRepository.save(comment);
        result = true;

        // Mapping sang DTO để broadcast
        CommentDTO response = new CommentDTO();
        response.setPost(comment.getPost());
        response.setAuthorName(comment.getAuthor().getName());
        response.setContent("Bình luận đã bị xóa"); // hoặc để trống tuỳ ý
        response.setCommentTime(comment.getCommentTime());
        response.setAvatar(comment.getAuthor().getAvatar());
        response.setStatus(comment.getStatus());

        // Broadcast realtime xoá comment
        notificationService.broadcastDeletedComment(comment.getPost().getId(), response);
        return result;
    }


    // Lấy comment theo post
    public CommentPageResponse getCommentsByPostId(Integer postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("commentTime").descending());

        // Chỉ lấy comment có status = ACTIVE
        Page<Comment> commentPage = commentRepository.findByPost_IdAndStatus(postId, Status.ACTIVE, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .authorId(c.getAuthor().getId())
                        .authorName(c.getAuthor().getUsername())
                        .authorAvatar(c.getAuthor().getAvatar())
                        .content(c.getContent())
                        .commentTime(c.getCommentTime())
                        .build()
                ).toList();

        return CommentPageResponse.builder()
                .content(commentResponses)
                .pageNumber(commentPage.getNumber())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .build();
    }



    // Lấy comment theo id
    public CommentResponse getCommentById(Integer id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (comment.getStatus() != Status.ACTIVE) {
            throw new AppException(ErrorCode.COMMENT_NOT_EXISTED); // hoặc trả về null
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .authorId(comment.getAuthor().getId().toString()) // giả sử User có id kiểu Integer/UUID
                .authorName(comment.getAuthor().getUsername())
                .authorAvatar(comment.getAuthor().getAvatar())
                .content(comment.getContent())
                .commentTime(comment.getCommentTime()) // giả sử trong Comment có field createdAt
                .build();
    }

}
