package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.CategoryRequest;
import com.arthurisidoro.personal_finance_api.dto.response.CategoryResponse;
import com.arthurisidoro.personal_finance_api.entity.Category;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.mapper.CategoryMapper;
import com.arthurisidoro.personal_finance_api.repository.CategoryRepository;
import com.arthurisidoro.personal_finance_api.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
                            CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUser(currentUser);

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    public List<CategoryResponse> findAll() {
        Long userId = currentUserService.getCurrentUserId();
        return categoryRepository.findByUserId(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findOwnedOrThrow(id);
        category.setName(request.getName());
        category.setType(request.getType());

        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Category category = findOwnedOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findOwnedOrThrow(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}