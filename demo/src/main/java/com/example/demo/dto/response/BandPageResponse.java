package com.example.demo.dto.response;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants(level = AccessLevel.PRIVATE)
public class BandPageResponse {
    private List<BandResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
