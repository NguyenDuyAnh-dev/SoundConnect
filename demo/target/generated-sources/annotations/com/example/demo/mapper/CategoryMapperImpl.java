package com.example.demo.mapper;

import com.example.demo.dto.request.CategoryRequest;
import com.example.demo.dto.request.CategoryUpdateRequest;
import com.example.demo.dto.response.CategoryGetAllResponse;
import com.example.demo.dto.response.CategoryResponse;
import com.example.demo.entity.Category;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T11:08:44+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toCategory(CategoryRequest categoryRequest) {
        if ( categoryRequest == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( categoryRequest.getName() );
        category.description( categoryRequest.getDescription() );

        return category.build();
    }

    @Override
    public List<CategoryGetAllResponse> toCategoryGetAllResponse(List<Category> categories) {
        if ( categories == null ) {
            return null;
        }

        List<CategoryGetAllResponse> list = new ArrayList<CategoryGetAllResponse>( categories.size() );
        for ( Category category : categories ) {
            list.add( categoryToCategoryGetAllResponse( category ) );
        }

        return list;
    }

    @Override
    public CategoryResponse toCategoryResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setId( category.getId() );
        categoryResponse.setName( category.getName() );
        categoryResponse.setDescription( category.getDescription() );
        categoryResponse.setStatus( category.getStatus() );
        categoryResponse.setCreatedAt( category.getCreatedAt() );

        return categoryResponse;
    }

    @Override
    public void updateCategoryFromRequest(Category category, CategoryUpdateRequest request) {
        if ( request == null ) {
            return;
        }

        category.setName( request.getName() );
        category.setDescription( request.getDescription() );
        category.setStatus( request.getStatus() );
    }

    protected CategoryGetAllResponse categoryToCategoryGetAllResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryGetAllResponse categoryGetAllResponse = new CategoryGetAllResponse();

        categoryGetAllResponse.setId( category.getId() );
        categoryGetAllResponse.setName( category.getName() );
        categoryGetAllResponse.setDescription( category.getDescription() );
        categoryGetAllResponse.setStatus( category.getStatus() );

        return categoryGetAllResponse;
    }
}
