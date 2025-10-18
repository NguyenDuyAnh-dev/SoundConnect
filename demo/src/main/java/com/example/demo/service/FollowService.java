package com.example.demo.service;

import com.example.demo.dto.request.FollowRequest;
import com.example.demo.dto.response.FollowResponse;
import com.example.demo.dto.response.FollowerResponse;
import com.example.demo.dto.response.PagedFollowerResponse;
import com.example.demo.entity.Band;
import com.example.demo.entity.User;
import com.example.demo.entity.Venue;
import com.example.demo.repository.BandRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VenueRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowService {
    UserRepository userRepository;
    BandRepository bandRepository;
    VenueRepository venueRepository;

    // --- FOLLOW / UNFOLLOW BAND ---
    @Transactional
    public FollowResponse followBand(Integer bandId, FollowRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band not found"));

        boolean added = user.getFollowedBands().add(band);
        userRepository.save(user);

        String message = added ? "Followed band successfully" : "Already following this band";

        return FollowResponse.builder()
                .message(message)
                .totalFollowers(band.getFollowers().size() + (added ? 1 : 0))
                .build();
    }

    @Transactional
    public FollowResponse unfollowBand(Integer bandId, FollowRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band not found"));

        boolean removed = user.getFollowedBands().remove(band);
        userRepository.save(user);

        String message = removed ? "Unfollowed band successfully" : "You are not following this band";

        return FollowResponse.builder()
                .message(message)
                .totalFollowers(band.getFollowers().size() - (removed ? 1 : 0))
                .build();
    }

    // --- FOLLOW / UNFOLLOW VENUE ---
    @Transactional
    public FollowResponse followVenue(Integer venueId, FollowRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        boolean added = user.getFollowedVenues().add(venue);
        userRepository.save(user);

        String message = added ? "Followed venue successfully" : "Already following this venue";

        return FollowResponse.builder()
                .message(message)
                .totalFollowers(venue.getFollowers().size() + (added ? 1 : 0))
                .build();
    }

    @Transactional
    public FollowResponse unfollowVenue(Integer venueId, FollowRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        boolean removed = user.getFollowedVenues().remove(venue);
        userRepository.save(user);

        String message = removed ? "Unfollowed venue successfully" : "You are not following this venue";

        return FollowResponse.builder()
                .message(message)
                .totalFollowers(venue.getFollowers().size() - (removed ? 1 : 0))
                .build();
    }
    // --- Lấy follower của band, có phân trang ---
    public PagedFollowerResponse getBandFollowers(Integer bandId, int page, int size) {
        Band band = bandRepository.findById(bandId)
                .orElseThrow(() -> new RuntimeException("Band not found"));

        PageRequest pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByFollowedBands_Id(bandId, pageable);

        return PagedFollowerResponse.builder()
                .followers(userPage.getContent().stream().map(user ->
                        FollowerResponse.builder()
                                .userId(user.getId())
                                .username(user.getUsername())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .build()
                ).collect(Collectors.toList()))
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .build();
    }

    // --- Lấy follower của venue, có phân trang ---
    public PagedFollowerResponse getVenueFollowers(Integer venueId, int page, int size) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        PageRequest pageable = PageRequest.of(page, size);
        Page<com.example.demo.entity.User> userPage = userRepository.findByFollowedVenues_Id(venueId, pageable);

        return PagedFollowerResponse.builder()
                .followers(userPage.getContent().stream().map(user ->
                        FollowerResponse.builder()
                                .userId(user.getId())
                                .username(user.getUsername())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .build()
                ).collect(Collectors.toList()))
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .build();
    }

    // --- Đếm follower nhanh (không phân trang) ---
    public long countBandFollowers(Integer bandId) {
        return userRepository.countByFollowedBands_Id(bandId);
    }

    public long countVenueFollowers(Integer venueId) {
        return userRepository.countByFollowedVenues_Id(venueId);
    }

}
