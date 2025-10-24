package com.example.demo.mapper;

import com.example.demo.dto.request.SalePostCreateRequest;
import com.example.demo.dto.request.SalePostUpdateRequest;
import com.example.demo.dto.response.SaleImageResponse;
import com.example.demo.dto.response.SalePostForCategoryResponse;
import com.example.demo.entity.SaleImage;
import com.example.demo.entity.SalePost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalePostMapper {
    SalePostForCategoryResponse toSalePostForCategoryResponse(SalePost salePost);
    List<SalePostForCategoryResponse> toSalePostForCategoryResponses(List<SalePost> salePosts);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true) // nếu chưa xử lý images
    SalePost toSalePost(SalePostCreateRequest request);


    SaleImageResponse toSaleImageResponse(SaleImage saleImage);
    List<SaleImageResponse> toSaleImageResponses(List<SaleImage> images);

    // dùng cho update (copy field từ request sang entity)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true) // set thủ công từ repo
    void updateSalePostFromRequest(SalePostUpdateRequest request, @MappingTarget SalePost salePost);

}
