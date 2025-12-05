package com.example.demo.controller;

import com.example.demo.dto.request.FindOrCreateRoomRequest;
import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.dto.response.MessageDTO;
import com.example.demo.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Chat API", description = "Các API nhắn tin, phòng chat, upload ảnh")
@SecurityRequirement(name = "api")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // --- LẤY LỊCH SỬ TIN NHẮN ---
    @Operation(summary = "Lấy lịch sử tin nhắn")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<MessageDTO>> getChatHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessagesForRoom(roomId));
    }

    // --- TẠO/TÌM PHÒNG CHAT ---
    @Operation(summary = "Tìm hoặc tạo phòng chat 1-1")
    @PostMapping("/one-on-one")
    public ResponseEntity<ChatRoomResponse> getOrCreateOneOnOneRoom(@RequestBody FindOrCreateRoomRequest request) {
        if (request.getUserId1() == null || request.getUserId2() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(chatService.findOrCreateOneOnOneRoom(request.getUserId1(), request.getUserId2()));
    }

    // --- GỬI TIN NHẮN (TEXT + ẢNH) ---
    @Operation(summary = "Gửi tin nhắn (Text/Image)",
            description = "Dùng multipart/form-data. Có thể gửi text, ảnh hoặc cả hai.")
    @PostMapping(value = "/{roomId}/messages/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> uploadImage(
            @PathVariable Long roomId,
            @RequestParam String senderId,
            @Parameter(description = "File ảnh (Optional)")
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Parameter(description = "Nội dung tin nhắn (Optional)")
            @RequestParam(required = false) String content) throws IOException {

        // Validate: Phải có ít nhất 1 trong 2 (ảnh hoặc text)
        if ((image == null || image.isEmpty()) && (content == null || content.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setChatRoomId(roomId);
        messageDTO.setSenderId(senderId);
        messageDTO.setContent(content);

        // Lưu xuống DB
        MessageDTO savedMessage = chatService.saveMessage(messageDTO, image);

        // Gửi qua WebSocket cho realtime
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);

        return ResponseEntity.ok(savedMessage);
    }

    @Operation(summary = "Lấy danh sách phòng chat của user",
            description = "Trả về tất cả các phòng (1-1 và nhóm) mà user này đang tham gia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ChatRoomResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User không tồn tại")
    })
    @GetMapping("/user/{userId}/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(
            @Parameter(description = "ID của user (UUID)", required = true)
            @PathVariable String userId) {

        List<ChatRoomResponse> rooms = chatService.getRoomsForUser(userId);
        return ResponseEntity.ok(rooms);
    }
}