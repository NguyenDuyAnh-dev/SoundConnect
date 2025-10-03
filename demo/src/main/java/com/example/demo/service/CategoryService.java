package com.example.demo.service;

import com.example.demo.dto.response.CategoryGetAllResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.entity.Category;
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
        List<Category> categories = categoryRepository.findAll();
        List<CategoryGetAllResponse> response = categoryMapper.toCategoryGetAllResponse(categories);
        return response;
    }

    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        CategoryResponse categoryResponse = categoryMapper.toCategoryResponse(category);
        return categoryResponse;
    }

    // lay category bang id co phan trang salepoost (chua lam)

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists!");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Integer id, Category categoryDetails) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            category.setStatus(categoryDetails.getStatus());
            category.setCreatedAt(categoryDetails.getCreatedAt());
            return categoryRepository.save(category);
        }).orElseThrow(() -> new RuntimeException("Category not found with id " + id));
    }

    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}
