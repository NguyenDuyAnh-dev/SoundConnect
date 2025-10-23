package com.example.demo.controller;

import com.example.demo.dto.request.BandRequest;
import com.example.demo.dto.response.BandPageResponse;
import com.example.demo.dto.response.BandResponse;
import com.example.demo.service.BandService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class BandController {
    @Autowired
    BandService bandService;

    /**
     *  Tạo band mới (người dùng hiện tại là leader)
     */
    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity createBand(
            @RequestParam String username,
            @ModelAttribute BandRequest request) {
        BandResponse response = bandService.createBand(username, request);
        return ResponseEntity.ok(response);
    }

    /**
     *  Lấy tất cả band đang hoạt động (có phân trang)
     */
    @GetMapping
    public ResponseEntity getAllBands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BandPageResponse response = bandService.getAllBands(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Tìm kiếm band theo từ khóa (có phân trang)
     */
    @GetMapping("/search")
    public ResponseEntity searchBands(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BandPageResponse response = bandService.searchBands(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Lấy chi tiết band
     */
    @GetMapping("/{bandId}")
    public ResponseEntity getBandById(@PathVariable Integer bandId) {
        BandResponse response = bandService.getBandById(bandId);
        return ResponseEntity.ok(response);
    }

    /**
     *  Cập nhật thông tin band (chỉ leader mới được sửa)
     */
    @PutMapping("/{bandId}/update")
    public ResponseEntity updateBand(
            @PathVariable Integer bandId,
            @RequestParam String username,
            @ModelAttribute BandRequest request) {
        BandResponse response = bandService.updateBand(bandId, username, request);
        return ResponseEntity.ok(response);
    }

    /**
     *  Xóa hoặc giải tán band (chỉ leader)
     */
    @DeleteMapping("/{bandId}")
    public ResponseEntity deleteBand(
            @PathVariable Integer bandId,
            @RequestParam String username) {
        boolean result = bandService.deleteBand(bandId, username);
        if (result) {
            return ResponseEntity.ok("Band đã được giải tán thành công");
        } else {
            return ResponseEntity.badRequest().body("Không thể giải tán band này");
        }
    }
}
