package com.example.demo.service;

import com.example.demo.dto.request.PostRequest;
import com.example.demo.dto.response.PostPageResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    NotificationService notificationService;

    // Tạo post mới
    public PostResponse createPost(String username, PostRequest postRequest) {
        System.out.println("bat dau tim user");
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        System.out.println("da qua dc buoc tim user");

        // Tạo entity Post
        Post post = new Post();

        if(postRequest.getFile().getSize() > 40_000_000L){
            throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
        }

        // Upload file nếu có
        String mediaUrl = "";
        if (postRequest.getFile() != null && !postRequest.getFile().isEmpty()) {
            try {
                String contentType = postRequest.getFile().getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(postRequest.getFile());

                } else {
                    mediaUrl = cloudinaryService.uploadAudio(postRequest.getFile());
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }


        // Set dữ liệu cho post
        System.out.println("da qua dc buoc luu file");
        post.setAuthor(user);
        post.setContent(postRequest.getContent());
        post.setMedia(mediaUrl);
        post.setLocation(postRequest.getLocation());
        post.setAvailable(true);
        post.setPostTime(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        // Mapping sang UserResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());


        // Mapping sang PostResponse
        PostResponse postResponse = new PostResponse();
        postResponse.setId(savedPost.getId());
        postResponse.setAuthor(userResponse);
        postResponse.setContent(savedPost.getContent());
        postResponse.setLocation(savedPost.getLocation());
        postResponse.setMedia(savedPost.getMedia());
        postResponse.setPostTime(savedPost.getPostTime());

        // Broadcast realtime cho tất cả user
        notificationService.broadcastNewPost(postResponse);

        return postResponse;
    }


    // Lấy tất cả post của một user
    public Page<PostPageResponse> getPostsByUser(String username, int page, int size) {
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Post> posts = postRepository.findByAuthor_UsernameOrderByPostTimeDesc(username, pageable);

        return posts.map(this::convertToResponse);
    }

    private PostPageResponse convertToResponse(Post post) {
        return PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getUsername())
                .content(post.getContent())
                .location(post.getLocation())
                .media(post.getMedia())
                .postTime(post.getPostTime())
                .reactionCount(post.getReactions().size())
                .commentCount(post.getCommentList().size())
                .build();
    }

    public Page<PostPageResponse> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());

        Page<Post> postPage = postRepository.findByContentContainingIgnoreCase(keyword, pageable);

        return postPage.map(post -> PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getUsername()) // hoặc tên hiển thị
                .content(post.getContent())
                .location(post.getLocation())
                .media(post.getMedia())
                .postTime(post.getPostTime())
                .reactionCount(post.getReactions() != null ? post.getReactions().size() : 0)
                .commentCount(post.getCommentList() != null ? post.getCommentList().size() : 0)
                .build()
        );
    }

    public Page<PostPageResponse> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());

        Page<Post> postPage = postRepository.findAll(pageable);

        return postPage.map(post -> PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getUsername()) // hoặc tên hiển thị
                .content(post.getContent())
                .location(post.getLocation())
                .media(post.getMedia())
                .postTime(post.getPostTime())
                .reactionCount(post.getReactions() != null ? post.getReactions().size() : 0)
                .commentCount(post.getCommentList() != null ? post.getCommentList().size() : 0)
                .build()
        );
    }


    // Lấy post theo id
//    public Optional<Post> getPostById(Integer id) {
//        return postRepository.findById(id);
//    }

    public Post getPostById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
    }

}
