package com.example.demo.controller;

import com.example.demo.dto.response.BandMemberPageResponse;
import com.example.demo.dto.response.BandMemberResponse;
import com.example.demo.service.BandMemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/band-members")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class BandMemberController {
    @Autowired
    BandMemberService bandMemberService;

    /**
     *  Thêm thành viên mới vào band (leader thêm hoặc duyệt yêu cầu join)
     */
    @PostMapping("/add")
    public ResponseEntity addMember(
            @RequestParam Integer bandId,
            @RequestParam String username,
            @RequestParam(defaultValue = "Member") String roleInBand) {
        BandMemberResponse response = bandMemberService.addMember(bandId, username, roleInBand);
        return ResponseEntity.ok(response);
    }

    /**
     *  Lấy danh sách thành viên của 1 band (phân trang)
     */
    @GetMapping("/band/{bandId}")
    public ResponseEntity getMembersByBand(
            @PathVariable Integer bandId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BandMemberPageResponse response = bandMemberService.getMembersByBand(bandId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Lấy tất cả band mà 1 user đang tham gia (phân trang)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity getBandsOfUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BandMemberPageResponse response = bandMemberService.getBandsOfUser(username, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     *  Cập nhật vai trò trong band (leader thay đổi role)
     */
    @PutMapping("/{bandMemberId}/role")
    public ResponseEntity updateRole(
            @PathVariable Integer bandMemberId,
            @RequestParam String newRole) {
        BandMemberResponse response = bandMemberService.updateRole(bandMemberId, newRole);
        return ResponseEntity.ok(response);
    }

    /**
     *  Xóa thành viên khỏi band (leader hoặc chính user rời band)
     */
    @DeleteMapping("/remove")
    public ResponseEntity removeMember(
            @RequestParam Integer bandId,
            @RequestParam String username) {
        boolean result = bandMemberService.removeMember(bandId, username);
        if (result) {
            return ResponseEntity.ok("Đã xóa thành viên khỏi band thành công");
        } else {
            return ResponseEntity.badRequest().body("Không thể xóa thành viên này");
        }
    }
}
