package com.example.demo.dto.response;

import com.example.demo.enums.Status;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class BandJoinRequestResponse {
    Integer id;
    Integer bandId;
    String bandName;
    String userId;
    String username;
    String message;
    Status status;
    LocalDateTime createdAt;
}
