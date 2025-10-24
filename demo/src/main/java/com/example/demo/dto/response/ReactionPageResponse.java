package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReactionPageResponse {
    private List<ReactionResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
