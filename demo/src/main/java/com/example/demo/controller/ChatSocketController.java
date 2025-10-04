package com.example.demo.controller;

import com.example.demo.dto.response.MessageDTO;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}/sendMessage")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload MessageDTO messageDTO) throws IOException {
        messageDTO.setChatRoomId(roomId);

        MessageDTO savedMessage = chatService.saveMessage(messageDTO, null);

        // Broadcast tới đúng roomId
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);
    }
}
