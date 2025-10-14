package com.example.demo.service;

import com.example.demo.dto.request.PostRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostPageResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Band;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import com.example.demo.entity.Venue;
import com.example.demo.enums.Status;
import com.example.demo.enums.Visibility;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.BandRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VenueRepository;
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
    BandRepository bandRepository;

    @Autowired
    VenueRepository venueRepository;

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
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRequest.getVisibility());
        post.setPostType(postRequest.getPostType());
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
        postResponse.setPostType(savedPost.getPostType());
        postResponse.setVisibility(savedPost.getVisibility());
        postResponse.setStatus(savedPost.getStatus());

        // Broadcast realtime cho tất cả user
        notificationService.broadcastNewPost(postResponse);

        return postResponse;
    }

    // Post cua band
    public PostResponse createPostForBand(String username, Integer bandId, PostRequest postRequest) {
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tìm band
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        // Upload file nếu có
        String mediaUrl = "";
        if (postRequest.getFile() != null && !postRequest.getFile().isEmpty()) {
            if (postRequest.getFile().getSize() > 40_000_000L) {
                throw new RuntimeException("File quá lớn, tối đa 40MB trên free plan");
            }
            try {
                String contentType = postRequest.getFile().getContentType();
                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(postRequest.getFile());
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(postRequest.getFile());
                }
            } catch (IOException e) {
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        // Tạo Post
        Post post = Post.builder()
                .author(user)
                .band(band)
                .content(postRequest.getContent())
                .media(mediaUrl)
                .location(postRequest.getLocation())
                .postTime(LocalDateTime.now())
                .postType(postRequest.getPostType())
                .visibility(postRequest.getVisibility())
                .status(Status.ACTIVE)
                .build();

        Post savedPost = postRepository.save(post);

        // Mapping UserResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        // Mapping PostResponse
        PostResponse postResponse = new PostResponse();
        postResponse.setId(savedPost.getId());
        postResponse.setAuthor(userResponse);
        postResponse.setContent(savedPost.getContent());
        postResponse.setLocation(savedPost.getLocation());
        postResponse.setMedia(savedPost.getMedia());
        postResponse.setPostTime(savedPost.getPostTime());
        postResponse.setPostType(savedPost.getPostType());
        postResponse.setVisibility(savedPost.getVisibility());
        postResponse.setStatus(savedPost.getStatus());

        // Broadcast realtime
        notificationService.broadcastNewPost(postResponse);

        return postResponse;
    }

    // post cua venue
    public PostResponse createPostForVenue(String username, Integer venueId, PostRequest postRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_NOT_EXISTED));

        // Upload file
        String mediaUrl = "";
        if (postRequest.getFile() != null && !postRequest.getFile().isEmpty()) {
            if (postRequest.getFile().getSize() > 40_000_000L) {
                throw new RuntimeException("File quá lớn, tối đa 40MB trên free plan");
            }
            try {
                String contentType = postRequest.getFile().getContentType();
                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(postRequest.getFile());
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(postRequest.getFile());
                }
            } catch (IOException e) {
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        // Tạo Post
        Post post = Post.builder()
                .author(user)
                .venue(venue)
                .content(postRequest.getContent())
                .media(mediaUrl)
                .location(postRequest.getLocation())
                .postTime(LocalDateTime.now())
                .postType(postRequest.getPostType())
                .visibility(postRequest.getVisibility() != null ? postRequest.getVisibility() : Visibility.PUBLIC)
                .status(Status.ACTIVE)
                .build();

        Post savedPost = postRepository.save(post);

        // Mapping sang UserResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        // Mapping PostResponse
        PostResponse postResponse = new PostResponse();
        postResponse.setId(savedPost.getId());
        postResponse.setAuthor(userResponse);
        postResponse.setContent(savedPost.getContent());
        postResponse.setLocation(savedPost.getLocation());
        postResponse.setMedia(savedPost.getMedia());
        postResponse.setPostTime(savedPost.getPostTime());
        postResponse.setPostType(savedPost.getPostType());
        postResponse.setVisibility(savedPost.getVisibility());
        postResponse.setStatus(savedPost.getStatus());

        // Broadcast
        notificationService.broadcastNewPost(postResponse);

        return postResponse;
    }



    // Update post
    public PostResponse updatePost(String username, Integer postId, PostUpdateRequest postRequest) {
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tìm post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Check quyền: chỉ author mới được update
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Nếu có file mới thì upload thay thế
        String mediaUrl = post.getMedia(); // giữ media cũ mặc định
        if (postRequest.getFile() != null && !postRequest.getFile().isEmpty()) {
            if (postRequest.getFile().getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

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

        // Cập nhật dữ liệu post
        post.setContent(postRequest.getContent());
        post.setLocation(postRequest.getLocation());
        post.setMedia(mediaUrl);
        post.setStatus(postRequest.getStatus() != null ? postRequest.getStatus() : post.getStatus());
        post.setPostType(postRequest.getPostType());
        post.setVisibility(postRequest.getVisibility());
        post.setPostTime(LocalDateTime.now()); // update lại thời gian

        Post updatedPost = postRepository.save(post);

        // Mapping sang UserResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        // Mapping sang PostResponse
        PostResponse postResponse = new PostResponse();
        postResponse.setId(updatedPost.getId());
        postResponse.setAuthor(userResponse);
        postResponse.setContent(updatedPost.getContent());
        postResponse.setLocation(updatedPost.getLocation());
        postResponse.setMedia(updatedPost.getMedia());
        postResponse.setPostTime(updatedPost.getPostTime());
        postResponse.setPostType(updatedPost.getPostType());
        postResponse.setVisibility(updatedPost.getVisibility());
        postResponse.setStatus(updatedPost.getStatus());

        // Broadcast cho tất cả client cập nhật feed
        notificationService.broadcastUpdatedPost(postResponse);

        return postResponse;
    }

    // Lấy tất cả post của 1 band theo visibility (PRIVATE hoặc PUBLIC)
    public Page<PostPageResponse> getPostsByBand(Integer bandId, Visibility visibility, int page, int size) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());

        // Giả sử repository có method: findByBandAndStatusAndVisibility
        Page<Post> posts = postRepository.findByBandAndStatusAndVisibility(band, Status.ACTIVE, visibility, pageable);

        // Map trực tiếp trong hàm
        return posts.map(post -> PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getFirstname() + " " + post.getAuthor().getLastname())
                .userId(post.getAuthor().getId())
                .username(post.getAuthor().getUsername())
                .userAvatar(post.getAuthor().getAvatar())
                .bandId(post.getBand().getId())
                .content(post.getContent())
                .location(post.getLocation())
                .media(post.getMedia())
                .postTime(post.getPostTime())
                .reactionCount(post.getReactions() != null ? post.getReactions().size() : 0)
                .commentCount(post.getCommentList() != null ? post.getCommentList().size() : 0)
                .build());
    }

    // Lấy tất cả post của 1 venue (ví dụ luôn public)
    public Page<PostPageResponse> getPostsByVenue(Integer venueId, int page, int size) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_NOT_EXISTED));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());

        Page<Post> posts = postRepository.findByVenueAndStatusAndVisibility(venue, Status.ACTIVE, Visibility.PUBLIC, pageable);

        return posts.map(post -> PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getFirstname() + " " + post.getAuthor().getLastname())
                .userId(post.getAuthor().getId())
                .username(post.getAuthor().getUsername())
                .userAvatar(post.getAuthor().getAvatar())
                .venueId(post.getVenue().getId())
                .content(post.getContent())
                .location(post.getLocation())
                .media(post.getMedia())
                .postTime(post.getPostTime())
                .reactionCount(post.getReactions() != null ? post.getReactions().size() : 0)
                .commentCount(post.getCommentList() != null ? post.getCommentList().size() : 0)
                .build());
    }


    // Xoá mềm post
    public boolean deletePost(String username, Integer postId) {
        boolean result;
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tìm post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // Check quyền
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Xoá mềm
        post.setStatus(Status.INACTIVE);
        postRepository.save(post);
        result = true;

        // Mapping sang PostResponse để broadcast
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setAuthor(userResponse);
        postResponse.setContent(post.getContent());
        postResponse.setLocation(post.getLocation());
        postResponse.setMedia(post.getMedia());
        postResponse.setPostTime(post.getPostTime());
        postResponse.setStatus(post.getStatus());
        postResponse.setPostType(post.getPostType());
        postResponse.setVisibility(post.getVisibility());

        // Broadcast cho tất cả client biết bài này đã bị xoá
        notificationService.broadcastDeletedPost(postResponse);

        return result;
    }



    // Lấy tất cả post của một user
    public Page<PostPageResponse> getPostsByUser(String username, int page, int size) {
        // Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Post> posts = postRepository.findByAuthor_UsernameAndStatusOrderByPostTimeDesc(username, Status.ACTIVE, pageable);

        return posts.map(this::convertToResponse);
    }

    private PostPageResponse convertToResponse(Post post) {
        return PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getUsername())
                .userId(post.getAuthor().getId())
                .username(post.getAuthor().getUsername())
                .userAvatar(post.getAuthor().getAvatar())
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

        Page<Post> postPage = postRepository.findByContentContainingIgnoreCaseAndStatus(keyword, Status.ACTIVE, pageable);

        return postPage.map(post -> PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getFirstname() + post.getAuthor().getLastname()) // hoặc tên hiển thị
                .userId(post.getAuthor().getId())
                .username(post.getAuthor().getUsername())
                .userAvatar(post.getAuthor().getAvatar())
                .venueId(post.getVenue().getId())
                .bandId(post.getBand().getId())
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

        Page<Post> postPage = postRepository.findByStatus(Status.ACTIVE, pageable);

        return postPage.map(post -> PostPageResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getFirstname() + post.getAuthor().getLastname()) // hoặc tên hiển thị
                .userId(post.getAuthor().getId())
                .username(post.getAuthor().getUsername())
                .userAvatar(post.getAuthor().getAvatar())
                .bandId(post.getBand() != null ? post.getBand().getId() : null)
                .venueId(post.getVenue() != null ? post.getVenue().getId() : null)
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
