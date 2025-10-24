package com.example.demo.mapper;

import com.example.demo.dto.request.CategoryRequest;
import com.example.demo.dto.request.CategoryUpdateRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.CategoryGetAllResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // Map từ request -> entity (khi tạo mới)
    Category toCategory(CategoryRequest categoryRequest);

    // Map từ entity -> response
    List<CategoryGetAllResponse> toCategoryGetAllResponse(List<Category> categories);

    CategoryResponse toCategoryResponse(Category category);

    // Map từ UpdateRequest -> entity (update)
    @Mapping(target = "id", ignore = true)          // id không cho sửa
    @Mapping(target = "createdAt", ignore = true)   // createdAt giữ nguyên
    @Mapping(target = "salePosts", ignore = true)   // không update salePosts từ request
    void updateCategoryFromRequest(@MappingTarget Category category, CategoryUpdateRequest request);
}
