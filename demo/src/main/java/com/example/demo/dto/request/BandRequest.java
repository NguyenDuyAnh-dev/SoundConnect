package com.example.demo.dto.request;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class BandRequest {
    String name;
    String genre;
    String description;
    MultipartFile avatar;
    MultipartFile coverImage;
}
