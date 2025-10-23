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
public class BandJoinRequestPageResponse {
    private List<BandJoinRequestResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
