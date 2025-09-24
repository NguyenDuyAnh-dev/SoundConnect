package com.example.demo.dto.request;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class PostRequest {
    String content ;
    String location;
    MultipartFile file;
}
