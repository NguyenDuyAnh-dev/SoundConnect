package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.dto.response.ChatRoomResponse;
import com.example.demo.dto.response.MessageDTO;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.enums.RoomType;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final Cloudinary cloudinary;

    // --- 1. TÌM HOẶC TẠO PHÒNG (Logic hiển thị chuẩn Facebook) ---
    @Transactional
    public ChatRoomResponse findOrCreateOneOnOneRoom(String userId1, String userId2) {
        // 1. Chặn tự chat
        if (userId1.equals(userId2)) {
            throw new AppException(ErrorCode.SELF_CHAT_NOT_ALLOWED);
        }

        // 2. Tìm phòng cũ
        Optional<ChatRoom> existingRoom = chatRoomRepository.findOneOnOneChatRoom(userId1, userId2);
        if (existingRoom.isPresent()) {
            // QUAN TRỌNG: Format lại tên/avatar theo góc nhìn của userId1
            return formatChatRoomResponse(existingRoom.get(), userId1);
        }

        // 3. Tạo phòng mới
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ChatRoom newRoom = new ChatRoom();
        newRoom.setName("Private Chat"); // Tên nội bộ DB
        newRoom.setType(RoomType.ONE_ON_ONE);

        Set<User> participants = new HashSet<>();
        participants.add(user1);
        participants.add(user2);
        newRoom.setParticipants(participants);

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        // QUAN TRỌNG: Format lại tên/avatar trước khi trả về
        return formatChatRoomResponse(savedRoom, userId1);
    }

    // --- 2. GỬI TIN NHẮN (Giữ nguyên logic của bạn) ---
    @Transactional
    public MessageDTO saveMessage(MessageDTO messageDTO, MultipartFile image) throws IOException {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ChatRoom chatRoom = chatRoomRepository.findById(messageDTO.getChatRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.CHATROOM_NOT_FOUND));

        Message message = new Message();
        message.setSender(sender);
        message.setChatRoom(chatRoom);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());

        // --- LOGIC UPLOAD CLOUDINARY ---
        if (image != null && !image.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));
                String imageUrl = (String) uploadResult.get("secure_url");
                message.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload ảnh lên Cloudinary: " + e.getMessage());
            }
        }

        Message savedMessage = messageRepository.save(message);
        return modelMapper.map(savedMessage, MessageDTO.class);
    }

    // --- 3. LẤY LỊCH SỬ TIN NHẮN ---
    public List<MessageDTO> getMessagesForRoom(Long roomId) {
        // Lưu ý: Đảm bảo Repository có hàm findByChatRoomIdOrderByTimestampAsc
        List<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(roomId);
        // Hoặc messageRepository.findByChatRoomIdOrderByTimestampAsc(roomId); nếu bạn đã định nghĩa

        return messages.stream()
                .map(msg -> modelMapper.map(msg, MessageDTO.class))
                .collect(Collectors.toList());
    }

    // --- 4. LẤY DANH SÁCH PHÒNG CỦA USER ---
    public List<ChatRoomResponse> getRoomsForUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<ChatRoom> rooms = chatRoomRepository.findChatRoomsByUserId(userId);

        return rooms.stream()
                // Tái sử dụng hàm helper để logic đồng nhất
                .map(room -> formatChatRoomResponse(room, userId))
                .collect(Collectors.toList());
    }

    // ==========================================
    // PRIVATE HELPER METHOD (Logic "Thần thánh")
    // ==========================================
    private ChatRoomResponse formatChatRoomResponse(ChatRoom room, String viewerId) {
        ChatRoomResponse response = modelMapper.map(room, ChatRoomResponse.class);

        if (RoomType.ONE_ON_ONE.equals(room.getType())) {
            // Tìm người "kia" (người không phải là viewerId)
            User partner = room.getParticipants().stream()
                    .filter(user -> !user.getId().equals(viewerId))
                    .findFirst()
                    .orElse(null);

            if (partner != null) {
                // Ưu tiên lấy Name, nếu null thì lấy Username
                String displayName = (partner.getName() != null && !partner.getName().isBlank())
                        ? partner.getName()
                        : partner.getUsername();

                response.setName(displayName);
                response.setAvatar(partner.getAvatar());
            }
        }
        // Nếu là GROUP thì giữ nguyên name/avatar gốc của phòng

        return response;
    }
}