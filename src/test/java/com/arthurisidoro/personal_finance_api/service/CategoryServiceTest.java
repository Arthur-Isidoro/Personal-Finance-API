package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.CategoryRequest;
import com.arthurisidoro.personal_finance_api.dto.response.CategoryResponse;
import com.arthurisidoro.personal_finance_api.entity.Category;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.mapper.CategoryMapper;
import com.arthurisidoro.personal_finance_api.repository.CategoryRepository;
import com.arthurisidoro.personal_finance_api.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void deveCriarCategoriaComSucesso() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Alimentação");
        request.setType("EXPENSE");

        User currentUser = new User();
        currentUser.setId(1L);

        Category savedCategory = new Category();
        savedCategory.setId(10L);
        savedCategory.setName("Alimentação");
        savedCategory.setType("EXPENSE");

        CategoryResponse expectedResponse = new CategoryResponse(10L, "Alimentação", "EXPENSE", null);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(expectedResponse);

        CategoryResponse response = categoryService.create(request);

        assertThat(response.getName()).isEqualTo("Alimentação");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarCategoriaDeOutroUsuario() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        CategoryRequest request = new CategoryRequest();
        request.setName("Categoria Alheia");

        assertThatThrownBy(() -> categoryService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deveExcluirCategoriaComSucesso() {
        User currentUser = new User();
        currentUser.setId(1L);

        Category category = new Category();
        category.setId(5L);
        category.setUser(currentUser);

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(category));

        categoryService.delete(5L);

        verify(categoryRepository).delete(category);
    }
}