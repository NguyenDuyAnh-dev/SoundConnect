package com.example.demo.service;


import com.example.demo.dto.request.InstrumentPostRequest;
import com.example.demo.dto.response.InstrumentPostResponse;
import com.example.demo.entity.InstrumentPost;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InstrumentPostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentPostService {

    private final InstrumentPostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // Phí đăng bài cố định
    private static final BigDecimal POST_FEE = new BigDecimal("4000");

    // Lấy ID của người dùng hiện tại từ Security Context (vì token subject là ID)
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // Tạo bài đăng mới (có trừ phí)
    @Transactional
    public InstrumentPostResponse createPost(InstrumentPostRequest request) {
        String sellerId = getCurrentUserId(); // Lấy userId ở đây
        // SỬA LỖI: Tìm người bán bằng ID thay vì username
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra số dư có đủ không
        if (seller.getBalance() == null || seller.getBalance().compareTo(POST_FEE) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        // 2. Trừ tiền vào tài khoản người bán
        seller.setBalance(seller.getBalance().subtract(POST_FEE));
        // Không cần gọi userRepository.save(seller) vì @Transactional sẽ tự động lưu thay đổi

        // 3. Tạo đối tượng bài đăng từ DTO
        InstrumentPost post = modelMapper.map(request, InstrumentPost.class);
        post.setSeller(seller); // Gán người bán
        post.setStatus(InstrumentPost.PostStatus.AVAILABLE); // Set trạng thái

        // 4. Lưu bài đăng vào database
        InstrumentPost savedPost = postRepository.save(post);

        // 5. Chuyển đổi entity đã lưu thành DTO để trả về
        return modelMapper.map(savedPost, InstrumentPostResponse.class);
    }

    // Cập nhật bài đăng đã có
    @Transactional
    public InstrumentPostResponse updatePost(Long postId, InstrumentPostRequest request) {
        String currentUserId = getCurrentUserId(); // Lấy userId ở đây
        // Tìm bài đăng cần cập nhật
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Kiểm tra xem người dùng hiện tại có phải là chủ bài đăng không (so sánh ID)
        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this post.");
        }

        // Dùng ModelMapper để cập nhật các trường từ request vào entity 'post'
        modelMapper.map(request, post);

        // Không cần gọi postRepository.save(post) vì @Transactional tự động lưu
        return modelMapper.map(post, InstrumentPostResponse.class);
    }


    // Xóa (gỡ) bài đăng
    @Transactional
    public void deletePost(Long postId) {
        String currentUserId = getCurrentUserId(); // Lấy userId ở đây
        // Tìm bài đăng cần xóa
        InstrumentPost post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Kiểm tra quyền sở hữu (so sánh ID)
        if (!post.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this post.");
        }

        // Xóa bài đăng
        postRepository.delete(post);
    }

    // Lấy danh sách tất cả bài đăng (không cần xác thực quyền sở hữu)
    public List<InstrumentPostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> modelMapper.map(post, InstrumentPostResponse.class))
                .collect(Collectors.toList());
    }
}

