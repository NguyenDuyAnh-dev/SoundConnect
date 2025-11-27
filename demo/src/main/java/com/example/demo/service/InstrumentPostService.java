package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.dto.request.InstrumentPostRequest;
import com.example.demo.dto.response.InstrumentPostResponse;
import com.example.demo.entity.InstrumentPost;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InstrumentPostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Thêm log
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Để log lỗi upload
public class InstrumentPostService {

    private final InstrumentPostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary; // <--- 1. Inject Cloudinary

    // Phí đăng bài cố định
    private static final BigDecimal POST_FEE = new BigDecimal("4000");

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // --- 2. Thêm tham số MultipartFile image vào hàm createPost ---
    @Transactional
    public InstrumentPostResponse createPost(InstrumentPostRequest request, MultipartFile image) {
        String sellerId = getCurrentUserId();
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra số dư
        if (seller.getBalance() == null || seller.getBalance().compareTo(POST_FEE) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        // 2. Trừ tiền
        seller.setBalance(seller.getBalance().subtract(POST_FEE));

        // 3. Map DTO
        InstrumentPost post = modelMapper.map(request, InstrumentPost.class);
        post.setSeller(seller);
        post.setStatus(InstrumentPost.PostStatus.AVAILABLE);

        // --- 4. LOGIC UPLOAD ẢNH (Mới) ---
        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                String url = (String) uploadResult.get("url");
                post.setImageUrl(url); // Lưu URL vào bài đăng
            } catch (IOException e) {
                log.error("Upload image failed for post", e);
                throw new RuntimeException("Upload image failed");
            }
        }
        // ---------------------------------

        InstrumentPost savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, InstrumentPostResponse.class);
    }

    // --- 3. Thêm tham số MultipartFile image vào hàm updatePost ---
    @Transactional
    public InstrumentPostResponse updatePost(Long postId, InstrumentPostRequest request, MultipartFile image) {
        String currentUserId = getCurrentUserId();

        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this post.");
        }

//        // Cập nhật thông tin text
//        modelMapper.map(request, post);
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.setTitle(request.getTitle());
        }

        if (request.getPrice() != null) {
            post.setPrice(request.getPrice());
        }

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }

        // --- 4. LOGIC CẬP NHẬT ẢNH (Nếu user gửi ảnh mới thì thay thế) ---
        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                String url = (String) uploadResult.get("url");
                post.setImageUrl(url); // Ghi đè URL cũ bằng URL mới
            } catch (IOException e) {
                log.error("Update image failed for post", e);
                throw new RuntimeException("Update image failed");
            }
        }
        // ------------------------------------------------------------------

        // postRepository.save(post); // Không cần thiết vì @Transactional tự lo, nhưng để cũng ko sao
        return modelMapper.map(post, InstrumentPostResponse.class);
    }

    @Transactional
    public void deletePost(Long postId) {
        String currentUserId = getCurrentUserId();
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this post.");
        }

        postRepository.delete(post);
    }

    public List<InstrumentPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, InstrumentPostResponse.class))
                .collect(Collectors.toList());
    }
}