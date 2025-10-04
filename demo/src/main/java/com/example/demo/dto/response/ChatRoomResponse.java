package com.example.demo.dto.response;
import com.example.demo.dto.request.UserDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomResponse {
        private Long id;
        private String name;
        private Set<UserDTO> participants;


}
