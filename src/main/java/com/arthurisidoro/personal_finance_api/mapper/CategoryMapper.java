package com.arthurisidoro.personal_finance_api.mapper;

import com.arthurisidoro.personal_finance_api.dto.response.CategoryResponse;
import com.arthurisidoro.personal_finance_api.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCreatedAt()
        );
    }
}