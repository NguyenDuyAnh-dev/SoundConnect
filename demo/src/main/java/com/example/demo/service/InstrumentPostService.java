package com.example.demo.service;

import com.example.demo.dto.request.InstrumentPostRequest;
import com.example.demo.dto.response.InstrumentPostResponse;
import com.example.demo.entity.InstrumentPost;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InstrumentPostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentPostService {

    private final InstrumentPostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private static final BigDecimal POST_FEE = new BigDecimal("4000");

    // Tạo bài đăng mới
    public InstrumentPostResponse createPost(InstrumentPostRequest request, String sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra số dư
        if (seller.getBalance() == null || seller.getBalance().compareTo(POST_FEE) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        // 2. Trừ tiền
        seller.setBalance(seller.getBalance().subtract(POST_FEE));
        userRepository.save(seller);

        // 3. Tạo bài đăng
        InstrumentPost post = modelMapper.map(request, InstrumentPost.class);
        post.setSeller(seller);
        post.setStatus(InstrumentPost.PostStatus.AVAILABLE);

        InstrumentPost savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, InstrumentPostResponse.class);
    }

    // Gỡ bài đăng
    public void deletePost(Long postId, String currentUserId) {
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)); // Giả sử bạn có ErrorCode này

        // KIỂM TRA QUYỀN: Chỉ chủ sở hữu mới được xóa bài
        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this post.");
        }

        postRepository.delete(post);
    }

    // Lấy tất cả bài đăng
    public List<InstrumentPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, InstrumentPostResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional // Đảm bảo toàn vẹn dữ liệu khi cập nhật
    public InstrumentPostResponse updatePost(Long postId, InstrumentPostRequest request, String currentUserId) {
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)); // Giả sử bạn có ErrorCode này

        // KIỂM TRA QUYỀN: Chỉ chủ sở hữu mới được cập nhật
        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this post.");
        }

        // Cập nhật các trường từ request DTO
        modelMapper.map(request, post);

        InstrumentPost updatedPost = postRepository.save(post);
        return modelMapper.map(updatedPost, InstrumentPostResponse.class);
    }
}

