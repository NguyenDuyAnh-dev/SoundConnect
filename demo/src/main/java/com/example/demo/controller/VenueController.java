package com.example.demo.controller;

import com.example.demo.dto.request.VenueRequest;
import com.example.demo.dto.response.VenuePageResponse;
import com.example.demo.dto.response.VenueResponse;
import com.example.demo.entity.Venue;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VenueRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.VenueService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class VenueController {
    VenueService venueService;
    UserService userService;

    @GetMapping
    public ResponseEntity getAllActiveVenues(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        VenuePageResponse response = venueService.getAllActiveVenues(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Tìm kiếm venue theo tên
     */
    @GetMapping("/search")
    public ResponseEntity searchVenues(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        VenuePageResponse response = venueService.searchVenues(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Lấy danh sách venue theo chủ sở hữu
     */
    @GetMapping("/owner/{username}")
    public ResponseEntity getVenuesByOwner(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        VenuePageResponse response = venueService.getActiveVenuesByOwner(username, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Tạo venue mới
     */
    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<VenueResponse> createVenue(
            @ModelAttribute VenueRequest request,
            @RequestParam String username) throws IOException {
        return ResponseEntity.ok(venueService.createVenue(request, username));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<VenueResponse> updateVenue(
            @PathVariable Integer id,
            @ModelAttribute VenueRequest request) throws IOException{
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    /**
     *  Ẩn hoặc bật lại venue
     */
    @DeleteMapping("/{id}")
    public ResponseEntity toggleVenueStatus(@PathVariable Integer id) {
        boolean result = venueService.toggleVenueStatus(id);
        return result
                ? ResponseEntity.ok("Đã xóa thành công")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Xóa thất bại");
    }
}
