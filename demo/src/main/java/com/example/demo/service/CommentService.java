package com.example.demo.service;

import com.example.demo.dto.request.CommentRequest;
import com.example.demo.dto.response.CommentDTO;
import com.example.demo.dto.response.CommentPageResponse;
import com.example.demo.dto.response.CommentResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
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

        Comment savedComment = commentRepository.save(comment);
        CommentDTO response = new CommentDTO();
        response.setPost(comment.getPost());
        response.setAuthorName(comment.getAuthor().getName());
        response.setContent(comment.getContent());
        response.setCommentTime(comment.getCommentTime());
        response.setAvatar(comment.getAuthor().getAvatar());

        return response;
    }


    // Lấy comment theo post
    public CommentPageResponse getCommentsByPostId(Integer postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("commentTime").descending());

        Page<Comment> commentPage = commentRepository.findByPost_Id(postId, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .authorId(c.getAuthor().getId())
                        .authorName(c.getAuthor().getName())
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
