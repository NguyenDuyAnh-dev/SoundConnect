package com.example.demo.mapper;

import com.example.demo.dto.request.CategoryRequest;
import com.example.demo.dto.response.CategoryGetAllResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // Map từ request -> entity (khi tạo mới)
    Category toCategory(CategoryRequest categoryRequest);

    List<CategoryGetAllResponse> toCategoryGetAllResponse(List<Category> categories);

    CategoryResponse toCategoryResponse(Category category);

}
