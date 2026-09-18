package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.config.TempAuthConfig;
import com.arthurisidoro.personal_finance_api.dto.request.CategoryRequest;
import com.arthurisidoro.personal_finance_api.dto.response.CategoryResponse;
import com.arthurisidoro.personal_finance_api.entity.Category;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUser(buildTempUserReference());

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findByUserId(TempAuthConfig.TEMP_USER_ID)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOwnedOrThrow(id);
        category.setName(request.getName());
        category.setType(request.getType());

        Category updated = categoryRepository.save(category);
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findOwnedOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findOwnedOrThrow(Long id) {
        return categoryRepository.findByIdAndUserId(id, TempAuthConfig.TEMP_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    private com.arthurisidoro.personal_finance_api.entity.User buildTempUserReference() {
        com.arthurisidoro.personal_finance_api.entity.User user =
                new com.arthurisidoro.personal_finance_api.entity.User();
        user.setId(TempAuthConfig.TEMP_USER_ID);
        return user;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getCreatedAt()
        );
    }
}