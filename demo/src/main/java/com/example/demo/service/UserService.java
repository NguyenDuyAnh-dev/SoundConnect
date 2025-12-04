package com.example.demo.service;

import com.example.demo.constant.PredefinedRole;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper; // Import ModelMapper
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    ModelMapper modelMapper; // Thay UserMapper bằng ModelMapper

    // Lưu ý: PasswordEncoder không nên để final nếu khởi tạo trực tiếp new() như cũ,
    // hoặc tốt hơn là Inject Bean từ Config. Ở đây tôi giữ nguyên logic của bạn nhưng bỏ final field injection cho nó.
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Dùng ModelMapper để map từ Request sang Entity
        User user = modelMapper.map(request, User.class);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(Status.ACTIVE);

        // Logic role cũ của bạn đang comment, tôi giữ nguyên
        // HashSet<String> roles = new HashSet<>();
        // roles.add(Role.USER.name());
        // user.setRoles(roles);

        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get all users");
        // ModelMapper không tự map List, phải dùng Stream
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }

    public UserResponse getUser(String userId) {
        log.info("In method get users");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return modelMapper.map(user, UserResponse.class);
    }

    public UserResponse getMyinfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findById(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return modelMapper.map(user, UserResponse.class);
    }

    @Transactional
    public UserResponse deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(Status.BANNED);
        userRepository.save(user);
        return modelMapper.map(user, UserResponse.class);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // QUAN TRỌNG: Map dữ liệu từ request VÀO user hiện có (update object)
        modelMapper.map(request, user);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse setUserStatus(String userId, Status status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setStatus(status);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse promoteToAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        var roles = new HashSet<>(user.getRoles());
        roleRepository.findById(PredefinedRole.ADMIN_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse demoteFromAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        var roles = new HashSet<>(user.getRoles());
        roles.removeIf(r -> PredefinedRole.ADMIN_ROLE.equals(r.getName()));
        user.setRoles(roles);
        return modelMapper.map(userRepository.save(user), UserResponse.class);
    }

    public List<UserResponse> searchUsers(String keyword) {
        List<User> users;
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword);
        }

        // Chuyển đổi List<User> sang List<UserResponse> bằng stream và modelMapper
        return users.stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());
    }

    public void updateTokenFCM(String username, String token) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setFcmToken(token);
        userRepository.save(user);
    }

    public String getTokenFCMByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getFcmToken)
                .orElse(null);
    }
}