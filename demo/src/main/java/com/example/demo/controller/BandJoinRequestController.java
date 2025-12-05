package com.example.demo.controller;

import com.example.demo.dto.response.BandJoinRequestPageResponse;
import com.example.demo.dto.response.BandJoinRequestResponse;
import com.example.demo.service.BandJoinRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/band-join-requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class BandJoinRequestController {
    @Autowired
    BandJoinRequestService bandJoinRequestService;

    // Gửi yêu cầu gia nhập band
    @PostMapping("/send")
    public ResponseEntity sendJoinRequest(
            @RequestParam String username,
            @RequestParam Integer bandId,
            @RequestParam(required = false) String message) {
        BandJoinRequestResponse response = bandJoinRequestService.sendJoinRequest(username, bandId, message);
        return ResponseEntity.ok(response);
    }

    //  Lấy danh sách yêu cầu của 1 band (phân trang)
    @GetMapping("/band/{bandId}")
    public ResponseEntity getRequestsForBand(
            @PathVariable Integer bandId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BandJoinRequestPageResponse response = bandJoinRequestService.getRequestsForBand(bandId, page, size);
        return ResponseEntity.ok(response);
    }

    //  Lấy danh sách yêu cầu của 1 user (phân trang)
    @GetMapping("/user/{username}")
    public ResponseEntity<BandJoinRequestPageResponse> getRequestsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BandJoinRequestPageResponse response = bandJoinRequestService.getRequestsByUser(username, page, size);
        return ResponseEntity.ok(response);
    }

    //  Duyệt yêu cầu (approve)
    @PutMapping("/approve/{requestId}")
    public ResponseEntity<BandJoinRequestResponse> approveRequest(@PathVariable Integer requestId) {
        BandJoinRequestResponse response = bandJoinRequestService.approveRequest(requestId);
        return ResponseEntity.ok(response);
    }

    //  Từ chối yêu cầu (reject)
    @PutMapping("/reject/{requestId}")
    public ResponseEntity<String> rejectRequest(@PathVariable Integer requestId) {
        boolean result = bandJoinRequestService.rejectRequest(requestId);
        if (result) {
            return ResponseEntity.ok("Từ chối yêu cầu thành công");
        } else {
            return ResponseEntity.badRequest().body("Không thể từ chối yêu cầu");
        }
    }
}
