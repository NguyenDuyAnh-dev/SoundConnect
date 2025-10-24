package com.example.demo.service;

import com.example.demo.dto.response.BandJoinRequestPageResponse;
import com.example.demo.dto.response.BandJoinRequestResponse;
import com.example.demo.entity.Band;
import com.example.demo.entity.BandJoinRequest;
import com.example.demo.entity.BandMember;
import com.example.demo.entity.User;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.BandJoinRequestRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BandJoinRequestService {
    @Autowired
    BandJoinRequestRepository bandJoinRequestRepository;
    @Autowired
    BandRepository bandRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BandMemberRepository bandMemberRepository;

    //  Gửi yêu cầu gia nhập band
    public BandJoinRequestResponse sendJoinRequest(String username, Integer bandId, String message) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new AppException(ErrorCode.BAND_NOT_EXISTED));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra trùng
        bandJoinRequestRepository.findByUser_UsernameAndBandId(username, bandId)
                .ifPresent(req -> {
                    req.setStatus(Status.INACTIVE);
                    bandJoinRequestRepository.save(req);
                });

        BandJoinRequest request = BandJoinRequest.builder()
                .band(band)
                .user(user)
                .message(message)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        BandJoinRequest savedRequest = bandJoinRequestRepository.save(request);

        // Trả về DTO response
        return BandJoinRequestResponse.builder()
                .id(savedRequest.getId())
                .bandId(savedRequest.getBand().getId())
                .bandName(savedRequest.getBand().getName())
                .userId(savedRequest.getUser().getId())
                .username(savedRequest.getUser().getUsername())
                .message(savedRequest.getMessage())
                .status(savedRequest.getStatus())
                .createdAt(savedRequest.getCreatedAt())
                .build();
    }


    //  Lấy danh sách yêu cầu của 1 band
    public BandJoinRequestPageResponse getRequestsForBand(Integer bandId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BandJoinRequest> requestPage = bandJoinRequestRepository.findByBandId(bandId, pageable);

        List<BandJoinRequestResponse> responses = requestPage.getContent().stream()
                .map(request -> BandJoinRequestResponse.builder()
                        .id(request.getId())
                        .bandId(request.getBand().getId())
                        .bandName(request.getBand().getName())
                        .userId(request.getUser().getId())
                        .username(request.getUser().getUsername())
                        .message(request.getMessage())
                        .status(request.getStatus())
                        .createdAt(request.getCreatedAt())
                        .build())
                .toList();

        return BandJoinRequestPageResponse.builder()
                .content(responses)
                .pageNumber(requestPage.getNumber() + 1)
                .totalElements(requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .build();
    }

    // Lấy danh sách yêu cầu của 1 user
    public BandJoinRequestPageResponse getRequestsByUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BandJoinRequest> requestPage = bandJoinRequestRepository.findByUser_Username(username, pageable);

        List<BandJoinRequestResponse> responses = requestPage.getContent().stream()
                .map(request -> BandJoinRequestResponse.builder()
                        .id(request.getId())
                        .bandId(request.getBand().getId())
                        .bandName(request.getBand().getName())
                        .userId(request.getUser().getId())
                        .username(request.getUser().getUsername())
                        .message(request.getMessage())
                        .status(request.getStatus())
                        .createdAt(request.getCreatedAt())
                        .build())
                .toList();

        return BandJoinRequestPageResponse.builder()
                .content(responses)
                .pageNumber(requestPage.getNumber() + 1)
                .totalElements(requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .build();
    }



    //  Duyệt yêu cầu (chủ band hoặc admin)
    public BandJoinRequestResponse approveRequest(Integer requestId) {
        BandJoinRequest request = bandJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

        if (request.getStatus() != Status.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý rồi");
        }

        // Thêm thành viên vào band
        BandMember member = BandMember.builder()
                .band(request.getBand())
                .user(request.getUser())
                .roleInBand("Member")
                .joinedAt(LocalDateTime.now())
                .status(Status.ACTIVE)
                .build();

        bandMemberRepository.save(member);

        // Cập nhật trạng thái yêu cầu
        request.setStatus(Status.ACCEPTED);
        BandJoinRequest savedRequest = bandJoinRequestRepository.save(request);

        // Trả về DTO
        return BandJoinRequestResponse.builder()
                .id(savedRequest.getId())
                .bandId(savedRequest.getBand().getId())
                .bandName(savedRequest.getBand().getName())
                .userId(savedRequest.getUser().getId())
                .username(savedRequest.getUser().getUsername())
                .message(savedRequest.getMessage())
                .status(savedRequest.getStatus())
                .createdAt(savedRequest.getCreatedAt())
                .build();
    }


    //  Từ chối yêu cầu
    public boolean rejectRequest(Integer requestId) {
        boolean result = false;
        BandJoinRequest request = bandJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không tồn tại"));

        if (request.getStatus() != Status.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý rồi");
        }

        request.setStatus(Status.REJECTED);
        bandJoinRequestRepository.save(request);
        result = true;
        return result;
    }

}
