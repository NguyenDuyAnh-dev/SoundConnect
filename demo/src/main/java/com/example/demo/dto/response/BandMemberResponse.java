package com.example.demo.dto.response;

import com.example.demo.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BandMemberResponse {
    Integer id;
    Integer bandId;
    String bandName;
    String userId;
    String username;
    String avatar;
    String roleInBand;
    LocalDateTime joinedAt;
    Status status;
}
