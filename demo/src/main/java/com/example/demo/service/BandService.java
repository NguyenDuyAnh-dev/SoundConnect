package com.example.demo.service;

import com.example.demo.dto.request.BandRequest;
import com.example.demo.dto.response.BandPageResponse;
import com.example.demo.dto.response.BandResponse;
import com.example.demo.entity.Band;
import com.example.demo.entity.BandMember;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.BandMemberRepository;
import com.example.demo.repository.BandRepository;
import com.example.demo.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BandService {
    @Autowired
    BandRepository bandRepository;
    @Autowired
    BandMemberRepository bandMemberRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CloudinaryService cloudinaryService;

    /**
     * 🪄 Tạo band mới (người dùng hiện tại là leader)
     */
    public BandResponse createBand(String username, BandRequest request) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String avatarUrl = null;
        String coverUrl = null;
        try {
            if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
                avatarUrl = cloudinaryService.uploadImage(request.getAvatar());
            }
            if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
                coverUrl = cloudinaryService.uploadImage(request.getCoverImage());
            }
        } catch (Exception e) {
            log.error("Lỗi upload ảnh band: {}", e.getMessage());
        }

        Band band = Band.builder()
                .name(request.getName())
                .genre(request.getGenre())
                .description(request.getDescription())
                .createdAt(LocalDate.now())
                .avatar(avatarUrl)
                .coverImage(coverUrl)
                .status(Status.ACTIVE)
                .build();

        Band saved = bandRepository.save(band);

        // Tự động thêm leader là người tạo band
        BandMember leader = BandMember.builder()
                .band(saved)
                .user(creator)
                .roleInBand("Leader")
                .joinedAt(java.time.LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();
        bandMemberRepository.save(leader);

        BandResponse response = new BandResponse();
        response.setId(band.getId());
        response.setName(band.getName());
        response.setGenre(band.getGenre());
        response.setDescription(band.getDescription());
        response.setCreatedAt(band.getCreatedAt());
        response.setAvatar(band.getAvatar());
        response.setCoverImage(band.getCoverImage());
        response.setStatus(band.getStatus());
        response.setMemberCount(1); // Leader là thành viên đầu tiên

        return response;
    }

    /**
     *  Lấy tất cả band đang hoạt động
     */
    public BandPageResponse getAllBands(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // page bắt đầu từ 1 cho dễ nhìn
        Page<Band> bandPage = bandRepository.findByStatus(Status.ACTIVE, pageable);

        List<BandResponse> bandResponses = bandPage.getContent().stream()
                .map(band -> BandResponse.builder()
                        .id(band.getId())
                        .name(band.getName())
                        .genre(band.getGenre())
                        .description(band.getDescription())
                        .createdAt(band.getCreatedAt())
                        .avatar(band.getAvatar())
                        .coverImage(band.getCoverImage())
                        .status(band.getStatus())
                        .memberCount(band.getMembers() != null ? band.getMembers().size() : 0)
                        .build())
                .collect(Collectors.toList());

        return BandPageResponse.builder()
                .content(bandResponses)
                .pageNumber(bandPage.getNumber() + 1)
                .totalElements(bandPage.getTotalElements())
                .totalPages(bandPage.getTotalPages())
                .build();
    }



    /**
     *  Tìm band theo từ khóa
     */
    public BandPageResponse searchBands(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Band> bandPage = bandRepository.findByNameContainingIgnoreCaseAndStatus(keyword, Status.ACTIVE, pageable);

        List<BandResponse> bandResponses = bandPage.getContent().stream()
                .map(band -> BandResponse.builder()
                        .id(band.getId())
                        .name(band.getName())
                        .genre(band.getGenre())
                        .description(band.getDescription())
                        .createdAt(band.getCreatedAt())
                        .avatar(band.getAvatar())
                        .coverImage(band.getCoverImage())
                        .status(band.getStatus())
                        .memberCount(band.getMembers() != null ? band.getMembers().size() : 0)
                        .build())
                .collect(Collectors.toList());

        return BandPageResponse.builder()
                .content(bandResponses)
                .pageNumber(bandPage.getNumber() + 1)
                .totalElements(bandPage.getTotalElements())
                .totalPages(bandPage.getTotalPages())
                .build();
    }



    /**
     *  Lấy chi tiết band
     */
    public BandResponse getBandById(Integer bandId) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));
        BandResponse response = new BandResponse();
        response.setId(band.getId());
        response.setName(band.getName());
        response.setGenre(band.getGenre());
        response.setDescription(band.getDescription());
        response.setCreatedAt(band.getCreatedAt());
        response.setAvatar(band.getAvatar());
        response.setCoverImage(band.getCoverImage());
        response.setStatus(band.getStatus());
        response.setMemberCount(band.getMembers() != null ? band.getMembers().size() : 0);
        return response;
    }

    /**
     *  Update thông tin band (chỉ leader mới được sửa)
     */
    public BandResponse updateBand(Integer bandId, String username, BandRequest request) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BandMember leader = bandMemberRepository
                .findByBand_IdAndRoleInBandAndStatus(bandId, "Leader", Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!leader.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        try {
            if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
                band.setAvatar(cloudinaryService.uploadImage(request.getAvatar()));
            }
            if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
                band.setCoverImage(cloudinaryService.uploadImage(request.getCoverImage()));
            }
        } catch (Exception e) {
            log.error("Lỗi upload ảnh band: {}", e.getMessage());
        }

        band.setName(request.getName());
        band.setGenre(request.getGenre());
        band.setDescription(request.getDescription());

        Band updated = bandRepository.save(band);
        BandResponse response = new BandResponse();
        response.setName(updated.getName());
        response.setGenre(updated.getGenre());
        response.setDescription(updated.getDescription());
        response.setCreatedAt(updated.getCreatedAt());
        response.setAvatar(updated.getAvatar());
        response.setCoverImage(updated.getCoverImage());
        response.setStatus(updated.getStatus());
        response.setMemberCount(updated.getMembers() != null ? updated.getMembers().size() : 0);
        return response;
    }

    /**
     *  Xóa (hoặc giải tán) band (leader thực hiện)
     */
    public boolean deleteBand(Integer bandId, String username) {
        boolean result = false;
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BandMember leader = bandMemberRepository
                .findByBand_IdAndRoleInBandAndStatus(bandId, "Leader", Status.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!leader.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        band.setStatus(Status.INACTIVE);
        bandRepository.save(band);


        // Set toàn bộ member INACTIVE luôn
        band.getMembers().forEach(m -> {
            m.setStatus(Status.INACTIVE);
            bandMemberRepository.save(m);
        });
        result = true;
        return result;
    }


}
