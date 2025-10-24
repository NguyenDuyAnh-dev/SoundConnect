package com.example.demo.controller;

import com.example.demo.dto.request.FindOrCreateRoomRequest;
import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.dto.response.MessageDTO;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
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
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<MessageDTO>> getChatHistory(@PathVariable Long roomId) {
        List<MessageDTO> messages = chatService.getMessagesForRoom(roomId);
        return ResponseEntity.ok(messages);
    }
    @PostMapping("/one-on-one")
    public ResponseEntity<ChatRoomResponse> getOrCreateOneOnOneRoom(@RequestBody FindOrCreateRoomRequest request) {
        String userId1 = request.getUserId1();
        String userId2 = request.getUserId2();

        if (userId1 == null || userId2 == null) {
            return ResponseEntity.badRequest().build();
        }

        ChatRoomResponse chatRoom = chatService.findOrCreateOneOnOneRoom(userId1, userId2);
        return ResponseEntity.ok(chatRoom);
    }
    @PostMapping("/{roomId}/messages/image")
    public ResponseEntity<MessageDTO> uploadImage(
            @PathVariable Long roomId,
            @RequestParam String senderId,
            @RequestParam MultipartFile image,
            @RequestParam(required = false) String content) throws IOException {

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setChatRoomId(roomId);
        messageDTO.setSenderId(senderId);
        messageDTO.setContent(content);

        MessageDTO savedMessage = chatService.saveMessage(messageDTO, image);

        // Gửi message qua WebSocket real-time
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);

        return ResponseEntity.ok(savedMessage);
    }
    @GetMapping("/user/{userId}/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getUserChatRooms(@PathVariable String userId) {
        // Bạn sẽ cần tạo phương thức 'getRoomsForUser' trong ChatService
        List<ChatRoomResponse> rooms = chatService.getRoomsForUser(userId);
        return ResponseEntity.ok(rooms);
    }
}


