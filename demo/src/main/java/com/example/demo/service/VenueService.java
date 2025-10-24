package com.example.demo.service;

import com.example.demo.dto.request.VenueRequest;
import com.example.demo.dto.response.VenuePageResponse;
import com.example.demo.dto.response.VenueResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.Venue;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VenueRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VenueService {
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CloudinaryService cloudinaryService;

    //  Lấy tất cả venue đang hoạt động
    public VenuePageResponse getAllActiveVenues(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Venue> venuePage = venueRepository.findByStatus(Status.ACTIVE, pageable);

        List<VenueResponse> responses = venuePage.getContent().stream()
                .map(venue -> VenueResponse.builder()
                        .id(venue.getId())
                        .name(venue.getName())
                        .location(venue.getLocation())
                        .description(venue.getDescription())
                        .avatarImage(venue.getAvatarImage())
                        .coverImage(venue.getCoverImage())
                        .contactInfo(venue.getContactInfo())
                        .ownerId(venue.getOwner().getId())
                        .ownerName(venue.getOwner().getUsername())
                        .status(venue.getStatus())
                        .build())
                .collect(Collectors.toList());

        return VenuePageResponse.builder()
                .content(responses)
                .pageNumber(venuePage.getNumber() + 1)
                .totalElements(venuePage.getTotalElements())
                .totalPages(venuePage.getTotalPages())
                .build();
    }



    //  Tìm kiếm venue theo tên
    public VenuePageResponse searchVenues(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Venue> venuePage = venueRepository.findByNameContainingIgnoreCaseAndStatus(keyword, Status.ACTIVE, pageable);

        List<VenueResponse> responses = venuePage.getContent().stream()
                .map(venue -> VenueResponse.builder()
                        .id(venue.getId())
                        .name(venue.getName())
                        .location(venue.getLocation())
                        .description(venue.getDescription())
                        .avatarImage(venue.getAvatarImage())
                        .coverImage(venue.getCoverImage())
                        .contactInfo(venue.getContactInfo())
                        .ownerId(venue.getOwner().getId())
                        .ownerName(venue.getOwner().getUsername())
                        .status(venue.getStatus())
                        .build())
                .collect(Collectors.toList());

        return VenuePageResponse.builder()
                .content(responses)
                .pageNumber(venuePage.getNumber() + 1)
                .totalElements(venuePage.getTotalElements())
                .totalPages(venuePage.getTotalPages())
                .build();
    }


    //  Lấy danh sách venue theo chủ sở hữu
    public VenuePageResponse getActiveVenuesByOwner(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Venue> venuePage = venueRepository.findByOwner_UsernameAndStatus(username, Status.ACTIVE, pageable);

        List<VenueResponse> responses = venuePage.getContent()
                .stream()
                .map(venue -> VenueResponse.builder()
                        .id(venue.getId())
                        .name(venue.getName())
                        .location(venue.getLocation())
                        .description(venue.getDescription())
                        .avatarImage(venue.getAvatarImage())
                        .coverImage(venue.getCoverImage())
                        .contactInfo(venue.getContactInfo())
                        .ownerId(venue.getOwner().getId())
                        .ownerName(venue.getOwner().getUsername())
                        .status(venue.getStatus())
                        .build())
                .collect(Collectors.toList());

        return VenuePageResponse.builder()
                .content(responses)
                .pageNumber(venuePage.getNumber() + 1)
                .totalElements(venuePage.getTotalElements())
                .totalPages(venuePage.getTotalPages())
                .build();
    }


    //  Tạo venue mới (dang bi bug do reaquest qua lon ko xu ly kip)
    public VenueResponse createVenue(VenueRequest request, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String imageUrl = null;
        String avatarUrl = null;
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadImage(request.getCoverImage());
                avatarUrl = cloudinaryService.uploadImage(request.getAvatarImage());
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        Venue venue = Venue.builder()
                .name(request.getName())
                .location(request.getLocation())
                .description(request.getDescription())
                .contactInfo(request.getContactInfo())
                .avatarImage(avatarUrl)
                .coverImage(imageUrl)
                .owner(owner)
                .status(Status.ACTIVE)
                .build();

        Venue saved = venueRepository.save(venue);

        // map ngay tại chỗ
        return VenueResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .description(saved.getDescription())
                .avatarImage(saved.getAvatarImage())
                .coverImage(saved.getCoverImage())
                .contactInfo(saved.getContactInfo())
                .ownerId(owner.getId())
                .ownerName(owner.getUsername())
                .status(saved.getStatus())
                .build();
    }

    public VenueResponse updateVenue(Integer id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VENUE_NOT_EXISTED));

        if (request.getName() != null) venue.setName(request.getName());
        if (request.getLocation() != null) venue.setLocation(request.getLocation());
        if (request.getDescription() != null) venue.setDescription(request.getDescription());
        if (request.getContactInfo() != null) venue.setContactInfo(request.getContactInfo());

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(request.getCoverImage());
                venue.setCoverImage(newImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        if (request.getAvatarImage() != null && !request.getAvatarImage().isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(request.getAvatarImage());
                venue.setAvatarImage(newImageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        Venue updated = venueRepository.save(venue);

        // map trực tiếp luôn
        return VenueResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .location(updated.getLocation())
                .description(updated.getDescription())
                .avatarImage(updated.getAvatarImage())
                .coverImage(updated.getCoverImage())
                .contactInfo(updated.getContactInfo())
                .ownerId(updated.getOwner().getId())
                .ownerName(updated.getOwner().getUsername())
                .status(updated.getStatus())
                .build();
    }



    //  Đổi trạng thái (ẩn/active)
    public boolean toggleVenueStatus(Integer id) {
        boolean result = false;
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng trà"));

        venue.setStatus(Status.INACTIVE);

        Venue updated = venueRepository.save(venue);
        result = true;
        VenueResponse response = new VenueResponse();
        response.setId(updated.getId());
        response.setName(updated.getName());
        response.setLocation(updated.getLocation());
        response.setDescription(updated.getDescription());
        response.setAvatarImage(updated.getAvatarImage());
        response.setCoverImage(updated.getCoverImage());
        response.setContactInfo(updated.getContactInfo());
        response.setOwnerId(updated.getOwner().getId());
        response.setOwnerName(updated.getOwner().getUsername());
        response.setStatus(updated.getStatus());
        return result;
    }


}
