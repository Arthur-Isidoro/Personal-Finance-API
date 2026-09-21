package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.config.TempAuthConfig;
import com.arthurisidoro.personal_finance_api.dto.request.TransactionRequest;
import com.arthurisidoro.personal_finance_api.dto.response.TransactionResponse;
import com.arthurisidoro.personal_finance_api.entity.Category;
import com.arthurisidoro.personal_finance_api.entity.Transaction;
import com.arthurisidoro.personal_finance_api.entity.TransactionType;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.BusinessRuleException;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.repository.CategoryRepository;
import com.arthurisidoro.personal_finance_api.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Category category = findOwnedCategoryOrThrow(request.getCategoryId());
        TransactionType type = parseType(request.getType());

        Transaction transaction = new Transaction();
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setType(type);
        transaction.setDate(request.getDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setCategory(category);
        transaction.setUser(buildTempUserReference());

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionRepository.findByUserId(TempAuthConfig.TEMP_USER_ID, pageable)
                .map(this::toResponse);
    }

    public TransactionResponse findById(Long id) {
        Transaction transaction = findOwnedOrThrow(id);
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        Transaction transaction = findOwnedOrThrow(id);
        Category category = findOwnedCategoryOrThrow(request.getCategoryId());
        TransactionType type = parseType(request.getType());

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setType(type);
        transaction.setDate(request.getDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setCategory(category);

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = findOwnedOrThrow(id);
        transactionRepository.delete(transaction);
    }

    private Transaction findOwnedOrThrow(Long id) {
        return transactionRepository.findByIdAndUserId(id, TempAuthConfig.TEMP_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }

    private Category findOwnedCategoryOrThrow(Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, TempAuthConfig.TEMP_USER_ID)
                .orElseThrow(() -> new BusinessRuleException(
                        "Categoria não encontrada ou não pertence ao usuário"));
    }

    private TransactionType parseType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Tipo inválido. Use INCOME ou EXPENSE");
        }
    }

    private User buildTempUserReference() {
        User user = new User();
        user.setId(TempAuthConfig.TEMP_USER_ID);
        return user;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getDate(),
                transaction.getPaymentMethod(),
                new TransactionResponse.CategorySummary(
                        transaction.getCategory().getId(),
                        transaction.getCategory().getName()
                )
        );
    }
}