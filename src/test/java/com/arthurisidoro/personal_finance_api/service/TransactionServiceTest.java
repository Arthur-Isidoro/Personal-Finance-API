package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.TransactionRequest;
import com.arthurisidoro.personal_finance_api.dto.response.TransactionResponse;
import com.arthurisidoro.personal_finance_api.entity.*;
import com.arthurisidoro.personal_finance_api.exception.BusinessRuleException;
import com.arthurisidoro.personal_finance_api.mapper.TransactionMapper;
import com.arthurisidoro.personal_finance_api.repository.CategoryRepository;
import com.arthurisidoro.personal_finance_api.repository.TransactionRepository;
import com.arthurisidoro.personal_finance_api.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void deveCriarTransacaoComSucesso() {
        TransactionRequest request = new TransactionRequest();
        request.setDescription("Almoço");
        request.setAmount(new BigDecimal("32.90"));
        request.setType("EXPENSE");
        request.setDate(LocalDate.of(2026, 9, 13));
        request.setCategoryId(2L);

        User currentUser = new User();
        currentUser.setId(1L);

        Category category = new Category();
        category.setId(2L);
        category.setName("Alimentação");

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(10L);

        TransactionResponse expectedResponse = new TransactionResponse(
                10L, "Almoço", new BigDecimal("32.90"), "EXPENSE",
                LocalDate.of(2026, 9, 13), null,
                new TransactionResponse.CategorySummary(2L, "Alimentação"));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(expectedResponse);

        TransactionResponse response = transactionService.create(request);

        assertThat(response.getAmount()).isEqualByComparingTo("32.90");
        assertThat(response.getDescription()).isEqualTo("Almoço");
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoPertenceAoUsuario() {
        TransactionRequest request = new TransactionRequest();
        request.setDescription("Compra suspeita");
        request.setAmount(new BigDecimal("50.00"));
        request.setType("EXPENSE");
        request.setDate(LocalDate.now());
        request.setCategoryId(999L);

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Categoria não encontrada");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deveLancarExcecaoQuandoTipoInvalido() {
        TransactionRequest request = new TransactionRequest();
        request.setDescription("Teste");
        request.setAmount(new BigDecimal("10.00"));
        request.setType("TIPO_INVALIDO");
        request.setDate(LocalDate.now());
        request.setCategoryId(1L);

        User currentUser = new User();
        currentUser.setId(1L);
        Category category = new Category();
        category.setId(1L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tipo inválido");
    }
}