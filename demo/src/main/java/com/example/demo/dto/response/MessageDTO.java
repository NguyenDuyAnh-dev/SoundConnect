package com.example.demo.dto.response;

import com.example.demo.dto.request.UserDTO;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO cho Message, đây là đối tượng sẽ được trao đổi giữa client và server.
 */
@Data
public class MessageDTO {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private String senderId; // Thông tin người gửi được lồng vào
    private Long chatRoomId;
    private String imageUrl;
}
