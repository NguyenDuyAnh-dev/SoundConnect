package com.example.demo.mapper;

import com.example.demo.dto.response.SalePostForCategoryResponse;
import com.example.demo.dto.response.SalePostResponse;
import com.example.demo.entity.SalePost;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalePostMapper {
    SalePostForCategoryResponse toSalePostForCategoryResponse(SalePost salePost);
    List<SalePostForCategoryResponse> toSalePostForCategoryResponses(List<SalePost> salePosts);
    SalePostResponse toSalePostResponse(SalePost salePost);

    List<SalePostResponse> toSalePostResponses(List<SalePost> salePosts);
}
