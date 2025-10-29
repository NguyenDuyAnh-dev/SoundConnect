package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.*;
import com.example.demo.entity.Band;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import com.example.demo.entity.Venue;
import com.example.demo.enums.PostType;
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
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    AIService aiService;

    // Tạo post mới
    public PostResponse createPost(String username, PostRequest postRequest) {
        System.out.println("Bắt đầu kiểm tra nội dung bằng AI...");
        String content = postRequest.getContent();
        boolean isValid = aiService.isMusicRelated(content);
        System.out.println(isValid);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CONTENT); // hoặc tạo ErrorCode mới
        }
        System.out.println("bat dau tim user");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        System.out.println("da qua dc buoc tim user");

        Post post = new Post();

        MultipartFile file = postRequest.getFile();
        String mediaUrl = "";

        //  Chỉ kiểm tra nếu file KHÔNG null và KHÔNG rỗng
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        post.setAuthor(user);
        post.setContent(postRequest.getContent());
        post.setMedia(mediaUrl);
        post.setLocation(postRequest.getLocation());
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRequest.getVisibility());
        post.setPostType(postRequest.getPostType());
        post.setPostTime(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

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

        notificationService.broadcastNewPost(postResponse);
        return postResponse;
    }


    // Post cua band
    public PostResponse createPostForBand(String username, Integer bandId, PostRequest postRequest) {
        System.out.println("Bắt đầu kiểm tra nội dung bằng AI...");
        String content = postRequest.getContent();
        boolean isValid = aiService.isMusicRelated(content);
        System.out.println(isValid);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CONTENT); // hoặc tạo ErrorCode mới
        }
        System.out.println("Bắt đầu tạo post cho band");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        Post post = new Post();

        MultipartFile file = postRequest.getFile();
        String mediaUrl = "";

        //  Chỉ upload nếu có file
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        post.setAuthor(user);
        post.setBand(band);
        post.setContent(postRequest.getContent());
        post.setMedia(mediaUrl);
        post.setLocation(postRequest.getLocation());
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRequest.getVisibility());
        post.setPostType(postRequest.getPostType());
        post.setPostTime(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

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

        notificationService.broadcastNewPost(postResponse);
        return postResponse;
    }


    // post cua venue
    public PostResponse createPostForVenue(String username, Integer venueId, PostRequest postRequest) {
        System.out.println("Bắt đầu kiểm tra nội dung bằng AI...");
        String content = postRequest.getContent();
        boolean isValid = aiService.isMusicRelated(content);
        System.out.println(isValid);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CONTENT); // hoặc tạo ErrorCode mới
        }
        System.out.println("Bắt đầu tạo post cho venue");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_NOT_EXISTED));

        Post post = new Post();

        MultipartFile file = postRequest.getFile();
        String mediaUrl = "";

        //  Chỉ upload nếu có file
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        post.setAuthor(user);
        post.setVenue(venue);
        post.setContent(postRequest.getContent());
        post.setMedia(mediaUrl);
        post.setLocation(postRequest.getLocation());
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRequest.getVisibility());
        post.setPostType(postRequest.getPostType());
        post.setPostTime(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

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

        notificationService.broadcastNewPost(postResponse);
        return postResponse;
    }

    // tao post tuyển thành viên cho band
    public PostRecruitingResponse createRecruitingPost(String username, Integer bandId, PostRecruitingRequest postRecruitingRequest) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        Post post = new Post();

        MultipartFile file = postRecruitingRequest.getFile();
        String mediaUrl = "";

        //  Chỉ upload nếu có file
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        post.setAuthor(user);
        post.setBand(band);
        post.setContent(postRecruitingRequest.getBandName());
        post.setBandName(postRecruitingRequest.getBandName());
        post.setBandGenre(postRecruitingRequest.getBandGenre());
        post.setBandDescription(postRecruitingRequest.getBandDescription());
        post.setBandRoles(postRecruitingRequest.getBandRoles());
        post.setBandExperience(postRecruitingRequest.getBandExperience());
        post.setHashtags(postRecruitingRequest.getHashtags());
        post.setMedia(mediaUrl);
        post.setLocation(postRecruitingRequest.getLocation());
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRecruitingRequest.getVisibility());
        post.setPostType(postRecruitingRequest.getPostType());
        post.setPostTime(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        PostRecruitingResponse recruitingResponse = new PostRecruitingResponse();
        recruitingResponse.setId(savedPost.getId());
        recruitingResponse.setAuthor(userResponse);
        recruitingResponse.setLocation(savedPost.getLocation());
        recruitingResponse.setMedia(savedPost.getMedia());
        recruitingResponse.setPostTime(savedPost.getPostTime());
        recruitingResponse.setVisibility(savedPost.getVisibility());
        recruitingResponse.setStatus(savedPost.getStatus());
        recruitingResponse.setPostType(savedPost.getPostType());
        recruitingResponse.setBandName(savedPost.getBandName());
        recruitingResponse.setBandGenre(savedPost.getBandGenre());
        recruitingResponse.setBandDescription(savedPost.getBandDescription());
        recruitingResponse.setBandRoles(savedPost.getBandRoles());
        recruitingResponse.setBandExperience(savedPost.getBandExperience());
        recruitingResponse.setHashtags(savedPost.getHashtags());
        return recruitingResponse;
    }

    //tạo post tìm band cho user
    public PostRecruitingUserResponse createRecruitingUserPost(String username, PostRecruitingUserRequest postRecruitingUserRequest) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = new Post();

        MultipartFile file = postRecruitingUserRequest.getFile();
        String mediaUrl = "";

        //  Chỉ upload nếu có file
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        post.setAuthor(user);
        post.setContent(""); // Không cần nội dung chung cho post tìm band
        post.setPlayerName(postRecruitingUserRequest.getPlayerName());
        post.setInstrument(postRecruitingUserRequest.getInstrument());
        post.setPlayerExperience(postRecruitingUserRequest.getPlayerExperience());
        post.setPlayerGenre(postRecruitingUserRequest.getPlayerGenre());
        post.setPlayerBio(postRecruitingUserRequest.getPlayerBio());
        post.setBandRoles(postRecruitingUserRequest.getBandRoles());
        post.setHashtags(postRecruitingUserRequest.getHashtags());
        post.setMedia(mediaUrl);
        post.setLocation(postRecruitingUserRequest.getLocation());
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRecruitingUserRequest.getVisibility());
        post.setPostType(postRecruitingUserRequest.getPostType());
        post.setPostTime(LocalDateTime.now());

        Post savedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        PostRecruitingUserResponse recruitingUserResponse = new PostRecruitingUserResponse();
        recruitingUserResponse.setId(savedPost.getId());
        recruitingUserResponse.setAuthor(userResponse);
        recruitingUserResponse.setLocation(savedPost.getLocation());
        recruitingUserResponse.setMedia(savedPost.getMedia());
        recruitingUserResponse.setPostTime(savedPost.getPostTime());
        recruitingUserResponse.setVisibility(savedPost.getVisibility());
        recruitingUserResponse.setStatus(savedPost.getStatus());
        recruitingUserResponse.setPostType(savedPost.getPostType());
        recruitingUserResponse.setPlayerName(savedPost.getPlayerName());
        recruitingUserResponse.setInstrument(savedPost.getInstrument());
        recruitingUserResponse.setPlayerExperience(savedPost.getPlayerExperience());
        recruitingUserResponse.setPlayerGenre(savedPost.getPlayerGenre());
        recruitingUserResponse.setPlayerBio(savedPost.getPlayerBio());
        recruitingUserResponse.setBandRoles(savedPost.getBandRoles());
        recruitingUserResponse.setHashtags(savedPost.getHashtags());
        return recruitingUserResponse;
    }

    // tạo post tuyển band cho venue
    public PostRecruitingVenueResponse createRecruitingVenuePost(String username, Integer venueId, PostRecruitingVenueRequest postRecruitingVenueRequest) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_NOT_EXISTED));

        Post post = new Post();

        MultipartFile file = postRecruitingVenueRequest.getFile();
        String mediaUrl = "";

        //  Chỉ upload nếu có file
        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        post.setAuthor(user);
        post.setVenue(venue);
        post.setContent(postRecruitingVenueRequest.getEventName());
        post.setMedia(mediaUrl);
        post.setLocation(postRecruitingVenueRequest.getLocation());
        post.setStatus(Status.ACTIVE);
        post.setVisibility(postRecruitingVenueRequest.getVisibility());
        post.setPostType(postRecruitingVenueRequest.getPostType());
        post.setPostTime(LocalDateTime.now());
        post.setEventName(postRecruitingVenueRequest.getEventName());
        post.setEventDateTime(postRecruitingVenueRequest.getEventDateTime());
        post.setEventStartTime(postRecruitingVenueRequest.getEventStartTime());
        post.setEventEndTime(postRecruitingVenueRequest.getEventEndTime());
        post.setEventGenre(postRecruitingVenueRequest.getEventGenre());
        post.setEventScale(postRecruitingVenueRequest.getEventScale());
        post.setEventDescription(postRecruitingVenueRequest.getEventDescription());
        post.setEventBenefits(postRecruitingVenueRequest.getEventBenefits());
        post.setEventDeadline(postRecruitingVenueRequest.getEventDeadline());
        post.setEventExpectedReply(postRecruitingVenueRequest.getEventExpectedReply());
        post.setEventApplicationRequirement(postRecruitingVenueRequest.getEventApplicationRequirement());
        post.setBandExperience(postRecruitingVenueRequest.getBandExperience());

        Post savedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        VenueResponse venueResponse = new VenueResponse();
        venueResponse.setId(venue.getId());
        venueResponse.setName(venue.getName());
        venueResponse.setLocation(venue.getLocation());
        venueResponse.setDescription(venue.getDescription());
        venueResponse.setCoverImage(venue.getCoverImage());
        venueResponse.setAvatarImage(venue.getAvatarImage());
        venueResponse.setContactInfo(venue.getContactInfo());
        venueResponse.setOwnerId(venue.getOwner().getId());
        venueResponse.setOwnerName(venue.getOwner().getFirstname() + " " + venue.getOwner().getLastname());
        venueResponse.setStatus(venue.getStatus());


        PostRecruitingVenueResponse recruitingVenueResponse = new PostRecruitingVenueResponse();
        recruitingVenueResponse.setId(savedPost.getId());
        recruitingVenueResponse.setAuthor(userResponse);
        recruitingVenueResponse.setLocation(savedPost.getLocation());
        recruitingVenueResponse.setMedia(savedPost.getMedia());
        recruitingVenueResponse.setPostTime(savedPost.getPostTime());
        recruitingVenueResponse.setVisibility(savedPost.getVisibility());
        recruitingVenueResponse.setStatus(savedPost.getStatus());
        recruitingVenueResponse.setPostType(savedPost.getPostType());
        recruitingVenueResponse.setEventName(savedPost.getEventName());
        recruitingVenueResponse.setEventDateTime(savedPost.getEventDateTime());
        recruitingVenueResponse.setEventStartTime(savedPost.getEventStartTime());
        recruitingVenueResponse.setEventEndTime(savedPost.getEventEndTime());
        recruitingVenueResponse.setEventGenre(savedPost.getEventGenre());
        recruitingVenueResponse.setEventScale(savedPost.getEventScale());
        recruitingVenueResponse.setEventDescription(savedPost.getEventDescription());
        recruitingVenueResponse.setEventBenefits(savedPost.getEventBenefits());
        recruitingVenueResponse.setEventDeadline(savedPost.getEventDeadline());
        recruitingVenueResponse.setEventExpectedReply(savedPost.getEventExpectedReply());
        recruitingVenueResponse.setEventApplicationRequirement(savedPost.getEventApplicationRequirement());
        recruitingVenueResponse.setBandExperience(savedPost.getBandExperience());
        recruitingVenueResponse.setVenue(venueResponse);
        return recruitingVenueResponse;
    }

    public PagedResponse<PostRecruitingResponse> getRecruitingPostsForBand(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());
        Page<Post> posts = postRepository.findByPostTypeAndStatusAndBandIsNotNull(PostType.RECRUITING, Status.ACTIVE, pageable);


        List<PostRecruitingResponse> content = posts.stream().map(post -> {
            User author = post.getAuthor();
            UserResponse userResponse = UserResponse.builder()
                    .id(author.getId())
                    .username(author.getUsername())
                    .firstName(author.getFirstname())
                    .lastName(author.getLastname())
                    .avatar(author.getAvatar())
                    .build();


            return PostRecruitingResponse.builder()
                    .id(post.getId())
                    .author(userResponse)
                    .location(post.getLocation())
                    .media(post.getMedia())
                    .postTime(post.getPostTime())
                    .visibility(post.getVisibility())
                    .status(post.getStatus())
                    .postType(post.getPostType())
                    .bandName(post.getBandName())
                    .bandGenre(post.getBandGenre())
                    .bandDescription(post.getBandDescription())
                    .bandRoles(post.getBandRoles())
                    .bandExperience(post.getBandExperience())
                    .hashtags(post.getHashtags())
                    .build();
        }).toList();

        return PagedResponse.<PostRecruitingResponse>builder()
                .content(content)
                .pageNumber(posts.getNumber())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }
    public PagedResponse<PostRecruitingUserResponse> getRecruitingPostsForUser(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());
        Page<Post> posts = postRepository.findByPostTypeAndStatus(PostType.RECRUITING, Status.ACTIVE, pageable);


        List<PostRecruitingUserResponse> content = posts.stream().map(post -> {
            User author = post.getAuthor();
            UserResponse userResponse = UserResponse.builder()
                    .id(author.getId())
                    .username(author.getUsername())
                    .firstName(author.getFirstname())
                    .lastName(author.getLastname())
                    .avatar(author.getAvatar())
                    .build();


            return PostRecruitingUserResponse.builder()
                    .id(post.getId())
                    .author(userResponse)
                    .location(post.getLocation())
                    .media(post.getMedia())
                    .postTime(post.getPostTime())
                    .visibility(post.getVisibility())
                    .status(post.getStatus())
                    .postType(post.getPostType())
                    .playerName(post.getPlayerName())
                    .instrument(post.getInstrument())
                    .playerExperience(post.getPlayerExperience())
                    .playerGenre(post.getPlayerGenre())
                    .playerBio(post.getPlayerBio())
                    .bandRoles(post.getBandRoles())
                    .hashtags(post.getHashtags())
                    .build();
        }).toList();

        return PagedResponse.<PostRecruitingUserResponse>builder()
                .content(content)
                .pageNumber(posts.getNumber())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    public PagedResponse<PostRecruitingVenueResponse> getRecruitingPostsForVenue(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postTime").descending());
        Page<Post> posts = postRepository.findByPostTypeAndStatusAndVenueIsNotNull(PostType.RECRUITING, Status.ACTIVE, pageable);

        List<PostRecruitingVenueResponse> content = posts.stream().map(post -> {
            User author = post.getAuthor();
            UserResponse userResponse = UserResponse.builder()
                    .id(author.getId())
                    .username(author.getUsername())
                    .firstName(author.getFirstname())
                    .lastName(author.getLastname())
                    .avatar(author.getAvatar())
                    .build();

            Venue venue = post.getVenue();
            VenueResponse venueResponse = null;

            //  Chỉ map nếu venue != null
            if (venue != null) {
                venueResponse = new VenueResponse();
                venueResponse.setId(venue.getId());
                venueResponse.setName(venue.getName());
                venueResponse.setLocation(venue.getLocation());
                venueResponse.setDescription(venue.getDescription());
                venueResponse.setCoverImage(venue.getCoverImage());
                venueResponse.setAvatarImage(venue.getAvatarImage());
                venueResponse.setContactInfo(venue.getContactInfo());
                venueResponse.setOwnerId(venue.getOwner().getId());
                venueResponse.setOwnerName(venue.getOwner().getFirstname() + " " + venue.getOwner().getLastname());
                venueResponse.setStatus(venue.getStatus());
            }

            return PostRecruitingVenueResponse.builder()
                    .id(post.getId())
                    .author(userResponse)
                    .location(post.getLocation())
                    .media(post.getMedia())
                    .postTime(post.getPostTime())
                    .visibility(post.getVisibility())
                    .status(post.getStatus())
                    .postType(post.getPostType())
                    .eventName(post.getEventName())
                    .eventDateTime(post.getEventDateTime())
                    .eventStartTime(post.getEventStartTime())
                    .eventEndTime(post.getEventEndTime())
                    .eventGenre(post.getEventGenre())
                    .eventScale(post.getEventScale())
                    .eventDescription(post.getEventDescription())
                    .eventBenefits(post.getEventBenefits())
                    .eventDeadline(post.getEventDeadline())
                    .eventExpectedReply(post.getEventExpectedReply())
                    .eventApplicationRequirement(post.getEventApplicationRequirement())
                    .bandExperience(post.getBandExperience())
                    .venue(venueResponse) //  Nếu venue null, nó sẽ truyền null mà không lỗi
                    .build();
        }).toList();

        return PagedResponse.<PostRecruitingVenueResponse>builder()
                .content(content)
                .pageNumber(posts.getNumber())
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }



    // Update post
    public PostResponse updatePost(String username, Integer postId, PostUpdateRequest postRequest) {
        System.out.println("Bắt đầu kiểm tra nội dung bằng AI...");
        String content = postRequest.getContent();
        boolean isValid = aiService.isMusicRelated(content);
        System.out.println(isValid);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CONTENT); // hoặc tạo ErrorCode mới
        }
        // 1. Tìm user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //  2. Tìm post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        //  3. Check quyền: chỉ author mới được update
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //  4. Upload file nếu có file mới
        String mediaUrl = post.getMedia(); // Giữ nguyên media cũ mặc định
        MultipartFile file = postRequest.getFile();

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 40_000_000L) {
                throw new EntityNotFoundException("File quá lớn, tối đa 40MB trên free plan");
            }

            try {
                String contentType = file.getContentType();
                log.info("Upload file contentType={}", contentType);

                if (contentType != null && contentType.startsWith("image")) {
                    mediaUrl = cloudinaryService.uploadImage(file);
                } else {
                    mediaUrl = cloudinaryService.uploadAudio(file);
                }
            } catch (IOException e) {
                log.error("Bug upload file: {}", e.getMessage(), e);
                throw new RuntimeException("Upload file thất bại: " + e.getMessage());
            }
        }

        //  5. Cập nhật dữ liệu post
        post.setContent(postRequest.getContent());
        post.setLocation(postRequest.getLocation());
        post.setMedia(mediaUrl);
        post.setStatus(postRequest.getStatus() != null ? postRequest.getStatus() : post.getStatus());
        post.setPostType(postRequest.getPostType());
        post.setVisibility(postRequest.getVisibility());
        post.setPostTime(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstname());
        userResponse.setLastName(user.getLastname());
        userResponse.setAvatar(user.getAvatar());

        //  7. Mapping sang PostResponse
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
