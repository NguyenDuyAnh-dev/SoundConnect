package com.example.demo.controller;

import com.example.demo.dto.request.FindOrCreateRoomRequest;
import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.dto.response.MessageDTO;
import com.example.demo.service.ChatService;
// Thêm các import của Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
//
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin("*")
// THÊM @Tag: Gom nhóm tất cả API trong file này vào một mục "Chat API"
@Tag(name = "Chat API", description = "Các API liên quan đến phòng chat và tin nhắn")
@SecurityRequirement(name = "api") // Giữ nguyên của bạn
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // --- 1. LẤY LỊCH SỬ TIN NHẮN ---
    @Operation(summary = "Lấy lịch sử tin nhắn của phòng chat",
            description = "Trả về toàn bộ danh sách tin nhắn của một phòng chat dựa theo roomId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử tin nhắn thành công",
                    // Chỉ rõ nội dung trả về là một MẢNG các MessageDTO
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MessageDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phòng chat với ID cung cấp", content = @Content)
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<MessageDTO>> getChatHistory(
            @Parameter(description = "ID của phòng chat cần lấy tin nhắn", required = true, example = "16")
            @PathVariable Long roomId) {

        List<MessageDTO> messages = chatService.getMessagesForRoom(roomId);
        return ResponseEntity.ok(messages);
    }

    // --- 2. TÌM HOẶC TẠO PHÒNG CHAT 1-1 ---
    @Operation(summary = "Tìm hoặc tạo phòng chat 1-với-1",
            description = "Cung cấp ID của 2 user. Nếu phòng chat 1-1 giữa họ đã tồn tại, trả về phòng đó. Nếu chưa, tạo phòng mới và trả về.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm hoặc tạo phòng thành công",
                    // Chỉ rõ nội dung trả về là một đối tượng ChatRoomResponse
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thiếu userId1 hoặc userId2 trong request body", content = @Content)
    })
    @PostMapping("/one-on-one")
    public ResponseEntity<ChatRoomResponse> getOrCreateOneOnOneRoom(
            // RequestBody cũng sẽ tự động được Swagger hiển thị (nếu DTO đã được @Schema)
            @RequestBody FindOrCreateRoomRequest request) {

        String userId1 = request.getUserId1();
        String userId2 = request.getUserId2();

        if (userId1 == null || userId2 == null) {
            return ResponseEntity.badRequest().build();
        }

        ChatRoomResponse chatRoom = chatService.findOrCreateOneOnOneRoom(userId1, userId2);
        return ResponseEntity.ok(chatRoom);
    }

    // --- 3. GỬI TIN NHẮN (TEXT/ẢNH) ---
    @Operation(summary = "Gửi tin nhắn (text hoặc ảnh) vào phòng chat",
            description = "Gửi tin nhắn mới. Phải có ít nhất 'content' (text) hoặc 'image' (ảnh). Endpoint này *luôn* dùng Content-Type là 'multipart/form-data'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi tin nhắn thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (ví dụ: không có cả content và image)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy User (senderId) hoặc ChatRoom (roomId)", content = @Content)
    })
    // Thêm 'consumes' để Swagger biết đây là endpoint UPLOAD FILE
    @PostMapping(value = "/{roomId}/messages/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> uploadImage(

            @Parameter(description = "ID của phòng chat", required = true, example = "16")
            @PathVariable Long roomId,

            @Parameter(description = "ID của người gửi (String UUID)", required = true, example = "a94d87c0-891b-4b68-8f05-8519a8acd517")
            @RequestParam String senderId,

            @Parameter(description = "File ảnh (không bắt buộc nếu đã có 'content')")
            @RequestParam(value = "image", required = false) MultipartFile image,

            @Parameter(description = "Nội dung text (không bắt buộc nếu đã có 'image')")
            @RequestParam(required = false) String content) throws IOException {

        if ((image == null || image.isEmpty()) && (content == null || content.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setChatRoomId(roomId);
        messageDTO.setSenderId(senderId);
        messageDTO.setContent(content);

        MessageDTO savedMessage = chatService.saveMessage(messageDTO, image);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);

        return ResponseEntity.ok(savedMessage);
    }

    // --- 4. LẤY DANH SÁCH PHÒNG CHAT CỦA USER ---
    @Operation(summary = "Lấy danh sách phòng chat của một user",
            description = "Trả về tất cả các phòng chat (cả 1-1 và nhóm) mà user này là thành viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách phòng thành công",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ChatRoomResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user với ID cung cấp", content = @Content)
    })
    @GetMapping("/user/{userId}/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(
            @Parameter(description = "ID của user cần lấy danh sách phòng", required = true, example = "a94d87c0-891b-4b68-8f05-8519a8acd517")
            @PathVariable String userId) {

        List<ChatRoomResponse> rooms = chatService.getRoomsForUser(userId);
        return ResponseEntity.ok(rooms);
    }
}