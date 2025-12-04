package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary; // <--- 1. Inject Cloudinary

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
     * Đã cập nhật để upload ảnh lên Cloudinary.
     */
    @Transactional
    public MessageDTO saveMessage(MessageDTO messageDTO, MultipartFile image) { // Bỏ throws IOException ở chữ ký hàm để xử lý gọn bên trong
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

        // --- 2. LOGIC UPLOAD CLOUDINARY ---
        if (image != null && !image.isEmpty()) {
            try {
                // Upload file lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());

                // Lấy URL ảnh online (https://...)
                String url = (String) uploadResult.get("url");
                message.setImageUrl(url);

            } catch (IOException e) {
                log.error("Upload image failed", e);
                // Bạn cần thêm mã lỗi UPLOAD_FILE_FAILED vào enum ErrorCode hoặc dùng lỗi chung
                throw new RuntimeException("Upload image failed");
            }
        }
        // ----------------------------------

        Message savedMessage = messageRepository.save(message);

        return convertToDto(savedMessage);
    }

    /**
     * Chuyển đổi Message Entity sang MessageDTO.
     */
    private MessageDTO convertToDto(Message message) {
        return modelMapper.map(message, MessageDTO.class);
    }

    private ChatRoomResponse convertToDto(ChatRoom chatRoom) {
        return modelMapper.map(chatRoom, ChatRoomResponse.class);
    }

    @Transactional
    public ChatRoomResponse findOrCreateOneOnOneRoom(String userId1, String userId2) {
        // Đảm bảo user không chat với chính mình
        if (userId1.equals(userId2)) {
            throw new AppException(ErrorCode.SELF_CHAT_NOT_ALLOWED);
        }

        // Sắp xếp ID để đảm bảo (A, B) giống (B, A)
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

            // Thêm participants
            newRoom.addParticipant(user1);
            newRoom.addParticipant(user2);

            ChatRoom savedRoom = chatRoomRepository.save(newRoom);
            return convertToDto(savedRoom);
        }
    }

    public List<ChatRoomResponse> getRoomsForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Lấy danh sách phòng từ entity User và sắp xếp
        return user.getChatRooms().stream()
                // Sắp xếp: phòng có tin nhắn cuối cùng mới nhất lên đầu
                .sorted(Comparator.comparing(
                        (ChatRoom room) -> {
                            // Nếu không có tin nhắn cuối, xếp xuống cuối
                            if (room.getLastMessage() == null) {
                                return LocalDateTime.MIN;
                            }
                            return room.getLastMessage().getTimestamp();
                        }
                ).reversed()) // .reversed() để mới nhất lên đầu
                .map(this::convertToDto) // Chuyển đổi sang DTO
                .collect(Collectors.toList());
    }
}