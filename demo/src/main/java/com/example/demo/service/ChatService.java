package com.example.demo.service;


import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.dto.response.MessageDTO;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ModelMapper modelMapper;

    /**
     * Lấy lịch sử tin nhắn của một phòng chat.
     */
    public List<MessageDTO> getMessagesForRoom(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new EntityNotFoundException("ChatRoom not found with id: " + roomId);
        }
        List<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lưu một tin nhắn mới được gửi từ client.
     */
    @Transactional
    public MessageDTO saveMessage(MessageDTO messageDTO, MultipartFile image) throws IOException {
        if (messageDTO.getSenderId() == null) {
            throw new IllegalArgumentException("Sender ID cannot be null");
        }

        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + messageDTO.getSenderId()));

        ChatRoom chatRoom = chatRoomRepository.findById(messageDTO.getChatRoomId())
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found with id: " + messageDTO.getChatRoomId()));

        Message message = new Message();
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());

        // Nếu có file image, lưu và set imageUrl
        if (image != null && !image.isEmpty()) {
            String filename = java.util.UUID.randomUUID() + "_" + image.getOriginalFilename();
            java.nio.file.Path path = java.nio.file.Paths.get("uploads/" + filename);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.copy(image.getInputStream(), path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            message.setImageUrl("/uploads/" + filename); // lưu đường dẫn file
        }

        Message savedMessage = messageRepository.save(message);

        return convertToDto(savedMessage);
    }

    /**
     * Phương thức pomocnicze để chuyển đổi Message Entity sang MessageDTO.
     */
    private MessageDTO convertToDto(Message message) {
        return modelMapper.map(message, MessageDTO.class);
    }
    private ChatRoomResponse convertToDto(ChatRoom chatRoom) {
        return modelMapper.map(chatRoom, ChatRoomResponse.class);
    }
    @Transactional
    public ChatRoomResponse findOrCreateOneOnOneRoom(String userId1, String userId2) {
        // Đảm bảo thứ tự để tránh trùng lặp, ví dụ (A,B) và (B,A) là một
        String user1IdSorted = userId1.compareTo(userId2) < 0 ? userId1 : userId2;
        String user2IdSorted = userId1.compareTo(userId2) < 0 ? userId2 : userId1;

        Optional<ChatRoom> existingRoom = chatRoomRepository.findOneOnOneChatRoom(user1IdSorted, user2IdSorted);

        if (existingRoom.isPresent()) {
            return convertToDto(existingRoom.get());
        } else {
            User user1 = userRepository.findById(userId1)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId1));
            User user2 = userRepository.findById(userId2)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId2));

            ChatRoom newRoom = new ChatRoom();
            newRoom.setName("Private Chat between " + user1.getUsername() + " and " + user2.getUsername());

            newRoom.addParticipant(user1);
            newRoom.addParticipant(user2);


            ChatRoom savedRoom = chatRoomRepository.save(newRoom);
            return convertToDto(savedRoom);
        }
    }

    /**
     * Hàm private để tạo một phòng chat 1-1 mới.
     */
    private ChatRoom createNewOneOnOneRoom(Long userId1, Long userId2) {
        User user1 = userRepository.findById(String.valueOf(userId1))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId1));
        User user2 = userRepository.findById(String.valueOf(userId2))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId2));

        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setName(user1.getUsername() + " - " + user2.getUsername()); // Tên phòng có thể tùy chỉnh

        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        newChatRoom.setParticipants(participants);

        return chatRoomRepository.save(newChatRoom);
    }
}


