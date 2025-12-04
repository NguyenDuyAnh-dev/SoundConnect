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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentPostService {

    private final InstrumentPostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary;

    // Cấu hình logic nghiệp vụ
    private static final BigDecimal MIN_PRICE_LIMIT = new BigDecimal("10000"); // Giá sàn 10k
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.04");   // Phí 4%

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * TẠO BÀI ĐĂNG (Có tính phí 4%)
     */
    @Transactional
    public InstrumentPostResponse createPost(InstrumentPostRequest request, MultipartFile image) {
        String sellerId = getCurrentUserId();
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra giá sàn (Price > 10.000)
        if (request.getPrice() == null || request.getPrice().compareTo(MIN_PRICE_LIMIT) <= 0) {
            throw new AppException(ErrorCode.PRICE_TOO_LOW);
        }

        // 2. Tính phí (Price * 0.04)
        BigDecimal postFee = request.getPrice().multiply(FEE_PERCENTAGE);

        // 3. Kiểm tra số dư
        if (seller.getBalance() == null || seller.getBalance().compareTo(postFee) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        // 4. Trừ tiền
        seller.setBalance(seller.getBalance().subtract(postFee));
        // userRepository.save(seller); // @Transactional tự lo việc này

        // 5. Map dữ liệu
        InstrumentPost post = modelMapper.map(request, InstrumentPost.class);
        post.setSeller(seller);
        post.setStatus(InstrumentPost.PostStatus.AVAILABLE);

        // 6. Upload ảnh (nếu có)
        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                String url = (String) uploadResult.get("url");
                post.setImageUrl(url);
            } catch (IOException e) {
                log.error("Create post: Upload image failed", e);
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }

        InstrumentPost savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, InstrumentPostResponse.class);
    }

    /**
     * CẬP NHẬT BÀI ĐĂNG
     */
    @Transactional
    public InstrumentPostResponse updatePost(Long postId, InstrumentPostRequest request, MultipartFile image) {
        String currentUserId = getCurrentUserId();

        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Kiểm tra chính chủ
        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // Cập nhật từng trường (chỉ cập nhật nếu có dữ liệu mới)
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.setTitle(request.getTitle());
        }
        if (request.getPrice() != null) {
            // Có thể thêm logic: Nếu sửa giá thấp quá thì chặn lại
            if (request.getPrice().compareTo(MIN_PRICE_LIMIT) <= 0) {
                throw new AppException(ErrorCode.PRICE_TOO_LOW);
            }
            post.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
        }

        // Cập nhật ảnh mới (nếu có)
        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
                String url = (String) uploadResult.get("url");
                post.setImageUrl(url);
            } catch (IOException e) {
                log.error("Update post: Upload image failed", e);
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }

        return modelMapper.map(post, InstrumentPostResponse.class);
    }

    /**
     * XÓA BÀI ĐĂNG
     */
    @Transactional
    public void deletePost(Long postId) {
        String currentUserId = getCurrentUserId();
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Kiểm tra chính chủ
        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        postRepository.delete(post);
    }

    /**
     * LẤY TẤT CẢ BÀI ĐĂNG
     */
    public List<InstrumentPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, InstrumentPostResponse.class))
                .collect(Collectors.toList());
    }

    /**
     * LẤY CHI TIẾT 1 BÀI ĐĂNG
     */
    public InstrumentPostResponse getPostById(Long postId) {
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        return modelMapper.map(post, InstrumentPostResponse.class);
    }
}