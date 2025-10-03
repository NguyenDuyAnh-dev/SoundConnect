package com.example.demo.service;

import com.example.demo.dto.request.CategoryRequest;
import com.example.demo.dto.request.CategoryUpdateRequest;
import com.example.demo.dto.response.CategoryGetAllResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.entity.Category;
import com.example.demo.enums.Status;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryMapper categoryMapper;

    public List<CategoryGetAllResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findByStatus(Status.ACTIVE);
        List<CategoryGetAllResponse> response = categoryMapper.toCategoryGetAllResponse(categories);
        return response;
    }

    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(category);
        return categoryResponse;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_DUPLICATED);
        }
        Category category = categoryMapper.toCategory(request);
        category.setStatus(Status.ACTIVE);
        category.setCreatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(category);

        return categoryResponse;
    }

    public CategoryResponse updateCategory(Integer id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException((ErrorCode.CATEGORY_NOT_EXISTED)));

        // Map dữ liệu từ request vào entity (không đè id, createdAt, salePosts)
        categoryMapper.updateCategoryFromRequest(category, categoryUpdateRequest);
        categoryRepository.save(category);
        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(category);
        return categoryResponse;
    }

    public boolean deleteCategory(Integer id) {
        boolean result = false;
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        category.setStatus(Status.INACTIVE);
        categoryRepository.save(category);
        result = true;
        return result;
    }
}
