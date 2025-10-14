package com.example.demo.dto.response;

import com.example.demo.enums.Status;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class VenueResponse {
    Integer id;
    String name;
    String location;
    String description;
    String coverImage;
    String avatarImage;
    String contactInfo;
    String ownerId;
    String ownerName;
    Status status;
}
