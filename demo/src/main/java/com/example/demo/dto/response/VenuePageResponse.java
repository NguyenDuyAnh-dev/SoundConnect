package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class VenuePageResponse {
    private List<VenueResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
