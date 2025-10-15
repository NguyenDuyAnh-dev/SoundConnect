package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.PostNoFileRequest;
import com.example.demo.dto.request.PostRequest;
import com.example.demo.dto.request.PostUpdateRequest;
import com.example.demo.dto.response.PostPageResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.enums.Visibility;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class PostController {
    PostService postService;

    @PostMapping(value = "/noFile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> createPostJson(
            @RequestParam String username,
            @RequestBody PostNoFileRequest postNoFileRequest) throws IOException {

        PostRequest postRequest = new PostRequest();
        postRequest.setContent(postNoFileRequest.getContent());
        postRequest.setLocation(postNoFileRequest.getLocation());
        postRequest.setVisibility(postNoFileRequest.getVisibility());
        postRequest.setPostType(postNoFileRequest.getPostType());
        // file để null
        return ResponseEntity.ok(postService.createPost(username, postRequest));
    }

    @PostMapping(value = "/withFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPostMultipart(
            @RequestParam String username,
            @ModelAttribute PostRequest postRequest) throws IOException {

        // file được bind bình thường
        return ResponseEntity.ok(postService.createPost(username, postRequest));
    }

    @PutMapping(value = "/noFile/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> updatePostJson(
            @PathVariable Integer postId,
            @RequestParam String username,
            @RequestBody PostNoFileRequest postUpdateNoFileRequest) throws IOException {

        PostUpdateRequest postRequest = new PostUpdateRequest();
        postRequest.setContent(postUpdateNoFileRequest.getContent());
        postRequest.setLocation(postUpdateNoFileRequest.getLocation());
        postRequest.setVisibility(postUpdateNoFileRequest.getVisibility());
        postRequest.setPostType(postUpdateNoFileRequest.getPostType());
        // file = null
        return ResponseEntity.ok(postService.updatePost(username, postId, postRequest));
    }

    @PutMapping(value = "/WithFile/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> updatePostMultipart(
            @PathVariable Integer postId,
            @RequestParam String username,
            @ModelAttribute PostUpdateRequest postRequest) throws IOException {

        return ResponseEntity.ok(postService.updatePost(username, postId, postRequest));
    }

    @PostMapping(value = "/band/noFile/{bandId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse createPostForBandJson(
            @RequestParam("username") String username,
            @PathVariable Integer bandId,
            @RequestBody PostNoFileRequest postNoFileRequest) {

        PostRequest postRequest = new PostRequest();
        postRequest.setContent(postNoFileRequest.getContent());
        postRequest.setLocation(postNoFileRequest.getLocation());
        postRequest.setVisibility(postNoFileRequest.getVisibility());
        postRequest.setPostType(postNoFileRequest.getPostType());
        // file = null
        return postService.createPostForBand(username, bandId, postRequest);
    }

    @PostMapping(value = "/band/withFile/{bandId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse createPostForBandMultipart(
            @RequestParam("username") String username,
            @PathVariable Integer bandId,
            @ModelAttribute PostRequest postRequest) {

        return postService.createPostForBand(username, bandId, postRequest);
    }

    @PostMapping(value = "/venue/noFile/{venueId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PostResponse createPostForVenueJson(
            @RequestParam("username") String username,
            @PathVariable Integer venueId,
            @RequestBody PostNoFileRequest postNoFileRequest) {

        PostRequest postRequest = new PostRequest();
        postRequest.setContent(postNoFileRequest.getContent());
        postRequest.setLocation(postNoFileRequest.getLocation());
        postRequest.setVisibility(postNoFileRequest.getVisibility());
        postRequest.setPostType(postNoFileRequest.getPostType());
        // file = null
        return postService.createPostForVenue(username, venueId, postRequest);
    }

    @PostMapping(value = "/venue/withFile/{venueId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostResponse createPostForVenueMultipart(
            @RequestParam("username") String username,
            @PathVariable Integer venueId,
            @ModelAttribute PostRequest postRequest) {

        return postService.createPostForVenue(username, venueId, postRequest);
    }



    // Lấy post của band (theo visibility)
    @GetMapping("/band/{bandId}")
    public Page<PostPageResponse> getPostsByBand(
            @PathVariable Integer bandId,
            @RequestParam Visibility visibility, // PRIVATE hoặc PUBLIC
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getPostsByBand(bandId, visibility, page, size);
    }

    // Lấy post của venue (luôn public)
    @GetMapping("/venue/{venueId}")
    public Page<PostPageResponse> getPostsByVenue(
            @PathVariable Integer venueId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getPostsByVenue(venueId, page, size);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity deletePost(
            @PathVariable Integer postId,
            @RequestParam String username) {

        boolean result = postService.deletePost(username, postId);
        String resultString = "";
        if(result){
            resultString = "Deleted successfully";
        }else{
            resultString= "Deleted Error";
        }
        return ResponseEntity.ok(resultString);
    }



    //  Lấy tất cả post của một user
    @GetMapping("/posts/users/{username}")
    public ResponseEntity getPostsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getPostsByUser(username, page, size));
    }

    //  Tìm post theo từ khóa trong caption
    @GetMapping("/search")
    public ResponseEntity searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostPageResponse> result = postService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    // API lấy toàn bộ bài viết (có phân trang, sort)
    @GetMapping
    public ResponseEntity<Page<PostPageResponse>> getAllPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PostPageResponse> response = postService.getAllPosts(page, size);
        return ResponseEntity.ok(response);
    }


    //  Lấy post theo id
    @GetMapping("/{id}")
    public ApiResponse<Post> getPostById(@PathVariable Integer id) {
        Post post = postService.getPostById(id);

        return ApiResponse.<Post>builder()
                .result(post)
                .build();
    }
}
