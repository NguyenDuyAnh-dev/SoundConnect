package com.example.demo.service;

import com.example.demo.dto.response.BandMemberPageResponse;
import com.example.demo.dto.response.BandMemberResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BandMemberService {
    @Autowired
    BandMemberRepository bandMemberRepository;
    @Autowired
    BandRepository bandRepository;
    @Autowired
    UserRepository userRepository;

    /**
     * Thêm 1 thành viên mới vào band (leader thêm hoặc duyệt yêu cầu join)
     */
    public BandMemberResponse addMember(Integer bandId, String username, String roleInBand) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra đã từng tham gia chưa
        BandMember existing = bandMemberRepository.findByBand_IdAndUser_Username(bandId, username)
                .orElse(null);

        BandMember saved;
        if (existing != null) {
            if (existing.getStatus() == Status.ACTIVE) {
                throw new AppException(ErrorCode.USER_ALREADY_IN_BAND);
            }
            // Nếu đã từng rời band → kích hoạt lại
            existing.setStatus(Status.ACTIVE);
            existing.setJoinedAt(LocalDateTime.now());
            existing.setRoleInBand(roleInBand);
            saved = bandMemberRepository.save(existing);
        } else {
            // Tạo mới nếu chưa từng tham gia
            BandMember bandMember = BandMember.builder()
                    .band(band)
                    .user(user)
                    .roleInBand(roleInBand)
                    .joinedAt(LocalDateTime.now())
                    .status(Status.ACTIVE)
                    .build();
            saved = bandMemberRepository.save(bandMember);
        }

        // Map trực tiếp sang response
        BandMemberResponse response = new BandMemberResponse();
        response.setId(saved.getId());
        response.setBandId(saved.getBand().getId());
        response.setBandName(saved.getBand().getName());
        response.setUserId(saved.getUser().getId());
        response.setUsername(saved.getUser().getUsername());
        response.setAvatar(saved.getUser().getAvatar());
        response.setRoleInBand(saved.getRoleInBand());
        response.setJoinedAt(saved.getJoinedAt());
        response.setStatus(saved.getStatus());

        return response;
    }

    /**
     * Lấy danh sách thành viên của 1 band
     */
    public BandMemberPageResponse getMembersByBand(Integer bandId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BandMember> memberPage = bandMemberRepository.findByBand_IdAndStatus(bandId, Status.ACTIVE, pageable);

        List<BandMemberResponse> responses = memberPage.getContent().stream()
                .map(member -> BandMemberResponse.builder()
                        .id(member.getId())
                        .bandId(member.getBand().getId())
                        .bandName(member.getBand().getName())
                        .userId(member.getUser().getId())
                        .username(member.getUser().getUsername())
                        .avatar(member.getUser().getAvatar())
                        .roleInBand(member.getRoleInBand())
                        .joinedAt(member.getJoinedAt())
                        .status(member.getStatus())
                        .build())
                .collect(Collectors.toList());

        return BandMemberPageResponse.builder()
                .content(responses)
                .pageNumber(memberPage.getNumber() + 1)
                .totalElements(memberPage.getTotalElements())
                .totalPages(memberPage.getTotalPages())
                .build();
    }



    /**
     * Lấy tất cả band mà 1 user đang tham gia
     */
    public BandMemberPageResponse getBandsOfUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BandMember> bandMemberPage = bandMemberRepository.findByUser_UsernameAndStatus(username, Status.ACTIVE, pageable);

        List<BandMemberResponse> responses = bandMemberPage.getContent().stream()
                .map(member -> BandMemberResponse.builder()
                        .id(member.getId())
                        .bandId(member.getBand().getId())
                        .bandName(member.getBand().getName())
                        .userId(member.getUser().getId())
                        .username(member.getUser().getUsername())
                        .avatar(member.getUser().getAvatar())
                        .roleInBand(member.getRoleInBand())
                        .joinedAt(member.getJoinedAt())
                        .status(member.getStatus())
                        .build())
                .collect(Collectors.toList());

        return BandMemberPageResponse.builder()
                .content(responses)
                .pageNumber(bandMemberPage.getNumber() + 1)
                .totalElements(bandMemberPage.getTotalElements())
                .totalPages(bandMemberPage.getTotalPages())
                .build();
    }


    /**
     * Cập nhật vai trò trong band (leader thay đổi role)
     */
    public BandMemberResponse updateRole(Integer bandMemberId, String newRole) {
        BandMember member = bandMemberRepository.findById(bandMemberId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_MEMBER_NOT_EXISTED));

        member.setRoleInBand(newRole);
        BandMember updated = bandMemberRepository.save(member);
        BandMemberResponse response = new BandMemberResponse();
        response.setId(updated.getId());
        response.setBandId(updated.getBand().getId());
        response.setBandName(updated.getBand().getName());
        response.setUserId(updated.getUser().getId());
        response.setUsername(updated.getUser().getUsername());
        response.setAvatar(updated.getUser().getAvatar());
        response.setRoleInBand(updated.getRoleInBand());
        response.setJoinedAt(updated.getJoinedAt());
        response.setStatus(updated.getStatus());
        return response;
    }

    /**
     * Xóa thành viên khỏi band (leader hoặc chính user rời band) (bug khi xoa lan 2 se ko the join lai doi fix)
     */
    public boolean removeMember(Integer bandId, String username) {
        BandMember member = bandMemberRepository.findByBand_IdAndUser_Username(bandId, username)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_MEMBER_NOT_EXISTED));

        if (member.getStatus() == Status.INACTIVE) {
            // Đã rời band rồi thì bỏ qua, không báo lỗi
            return true;
        }

        member.setStatus(Status.INACTIVE);
        bandMemberRepository.save(member);
        return true;
    }


}
