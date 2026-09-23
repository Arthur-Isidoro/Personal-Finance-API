package com.arthurisidoro.personal_finance_api.controller;

import com.arthurisidoro.personal_finance_api.config.SecurityConfig;
import com.arthurisidoro.personal_finance_api.dto.request.TransactionRequest;
import com.arthurisidoro.personal_finance_api.dto.response.TransactionResponse;
import com.arthurisidoro.personal_finance_api.exception.BusinessRuleException;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.security.JwtAuthFilter;
import com.arthurisidoro.personal_finance_api.security.JwtService;
import com.arthurisidoro.personal_finance_api.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

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
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar201AoCriarTransacao() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setDescription("Almoço");
        request.setAmount(new BigDecimal("32.90"));
        request.setType("EXPENSE");
        request.setDate(LocalDate.of(2026, 9, 13));
        request.setCategoryId(2L);

        TransactionResponse response = new TransactionResponse(
                1L, "Almoço", new BigDecimal("32.90"), "EXPENSE",
                LocalDate.of(2026, 9, 13), null,
                new TransactionResponse.CategorySummary(2L, "Alimentação"));

        when(transactionService.create(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Almoço"));
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar400QuandoCategoriaInvalida() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setDescription("Compra suspeita");
        request.setAmount(new BigDecimal("50.00"));
        request.setType("EXPENSE");
        request.setDate(LocalDate.now());
        request.setCategoryId(999L);

        when(transactionService.create(any(TransactionRequest.class)))
                .thenThrow(new BusinessRuleException("Categoria não encontrada ou não pertence ao usuário"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar200AoListarTransacoesPaginado() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L, "Almoço", new BigDecimal("32.90"), "EXPENSE",
                LocalDate.of(2026, 9, 13), null,
                new TransactionResponse.CategorySummary(2L, "Alimentação"));

        Page<TransactionResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

        when(transactionService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Almoço"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "arthur@teste.com")
    void deveRetornar404AoBuscarTransacaoInexistente() throws Exception {
        when(transactionService.findById(999L))
                .thenThrow(new ResourceNotFoundException("Transação não encontrada"));

        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound());
    }
}