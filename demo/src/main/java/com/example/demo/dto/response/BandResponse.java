package com.example.demo.dto.response;

import com.example.demo.enums.Status;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class BandResponse {
    Integer id;
    String name;
    String genre;
    String description;
    LocalDate createdAt;
    String avatar;
    String coverImage;
    Integer memberCount;
    Status status;
}
