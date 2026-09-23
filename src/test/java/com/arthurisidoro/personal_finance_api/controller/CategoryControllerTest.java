package com.arthurisidoro.personal_finance_api.controller;

import com.arthurisidoro.personal_finance_api.config.SecurityConfig;
import com.arthurisidoro.personal_finance_api.dto.request.CategoryRequest;
import com.arthurisidoro.personal_finance_api.dto.response.CategoryResponse;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.security.JwtAuthFilter;
import com.arthurisidoro.personal_finance_api.security.JwtService;
import com.arthurisidoro.personal_finance_api.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar201AoCriarCategoriaAutenticado() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Alimentação");
        request.setType("EXPENSE");

        CategoryResponse response = new CategoryResponse(1L, "Alimentação", "EXPENSE", LocalDateTime.now());

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alimentação"));
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar200AoListarCategorias() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Alimentação", "EXPENSE", LocalDateTime.now());

        when(categoryService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alimentação"));
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar404AoAtualizarCategoriaInexistente() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Categoria Inexistente");
        request.setType("EXPENSE");

        when(categoryService.update(any(Long.class), any(CategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Categoria não encontrada"));

        mockMvc.perform(put("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}