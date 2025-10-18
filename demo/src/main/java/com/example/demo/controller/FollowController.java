package com.example.demo.controller;

import com.example.demo.dto.request.FollowRequest;
import com.example.demo.dto.response.FollowResponse;
import com.example.demo.dto.response.PagedFollowerResponse;
import com.example.demo.service.FollowService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class FollowController {
    @Autowired
    FollowService followService;

    // ==================== BAND ====================

    @PostMapping("/band/{bandId}")
    public ResponseEntity followBand(@PathVariable Integer bandId, @RequestBody FollowRequest request) {
        FollowResponse followResponse = followService.followBand(bandId, request);
        return ResponseEntity.ok(followResponse);
    }

    @DeleteMapping("/band/{bandId}")
    public ResponseEntity unfollowBand(@PathVariable Integer bandId, @RequestBody FollowRequest request) {
        FollowResponse followResponse =  followService.unfollowBand(bandId, request);
        return ResponseEntity.ok(followResponse);
    }

    @GetMapping("/band/{bandId}/followers")
    public ResponseEntity getBandFollowers(
            @PathVariable Integer bandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedFollowerResponse pagedFollowerResponse = followService.getBandFollowers(bandId, page, size);
        return ResponseEntity.ok(pagedFollowerResponse);
    }

    @GetMapping("/band/{bandId}/followers/count")
    public long countBandFollowers(@PathVariable Integer bandId) {
        return followService.countBandFollowers(bandId);
    }

    // ==================== VENUE ====================

    @PostMapping("/venue/{venueId}")
    public FollowResponse followVenue(@PathVariable Integer venueId, @RequestBody FollowRequest request) {
        return followService.followVenue(venueId, request);
    }

    @DeleteMapping("/venue/{venueId}")
    public ResponseEntity unfollowVenue(@PathVariable Integer venueId, @RequestBody FollowRequest request) {
        FollowResponse followResponse = followService.unfollowVenue(venueId, request);
        return ResponseEntity.ok(followResponse);
    }

    @GetMapping("/venue/{venueId}/followers")
    public ResponseEntity getVenueFollowers(
            @PathVariable Integer venueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedFollowerResponse pagedFollowerResponse = followService.getVenueFollowers(venueId, page, size);
        return ResponseEntity.ok(pagedFollowerResponse);
    }

    @GetMapping("/venue/{venueId}/followers/count")
    public long countVenueFollowers(@PathVariable Integer venueId) {
        return followService.countVenueFollowers(venueId);
    }
}
