package com.example.demo.controller;

import com.example.demo.dto.request.ApiResponse;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class UserController {

     UserService userService;
    @PostMapping("/register")
    ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<User> response = new ApiResponse<>();
        response.setResult(userService.createUser(request));
        return response;

    }
    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
                var authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authenticated user: {}", authenticatedUser.getName());
        authenticatedUser.getAuthorities().forEach(grantedAuthority ->
            log.info("Granted authority: {}", grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }
    @PutMapping("/{userId}/status")
    ApiResponse<UserResponse> deleteUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.deleteUser(userId ))
                .build();
    }
    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId,@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    // Admin: set status (ACTIVE/INACTIVE/BANNED)
    @PutMapping("/{userId}/admin/status")
    ApiResponse<UserResponse> setUserStatus(@PathVariable String userId, @RequestParam Status status) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.setUserStatus(userId, status))
                .build();
    }

    // Admin: promote to ADMIN
    @PutMapping("/{userId}/admin/promote")
    ApiResponse<UserResponse> promote(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.promoteToAdmin(userId))
                .build();
    }

    // Admin: demote from ADMIN
    @PutMapping("/{userId}/admin/demote")
    ApiResponse<UserResponse> demote(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.demoteFromAdmin(userId))
                .build();
    }

    @GetMapping("/myinfo")
    ApiResponse<UserResponse> getMyinfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyinfo())
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchUsers(@RequestParam("keyword") String keyword) {
        List<UserResponse> foundUsers = userService.searchUsers(keyword);
        return ApiResponse.<List<UserResponse>>builder()
                .code(200) // Mặc định là 200 OK
                .message("Tìm kiếm người dùng thành công.")
                .result(foundUsers)
                .build();
    }

    @PatchMapping("/update-fcm-token/{username}")
    public ResponseEntity updateFcmToken(
            @RequestParam String username,
            @RequestParam String fcmToken) {
        userService.updateTokenFCM(username, fcmToken);
        return ResponseEntity.ok().build();
    }


}
