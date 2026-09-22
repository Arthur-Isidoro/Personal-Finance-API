package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.config.TempAuthConfig;
import com.arthurisidoro.personal_finance_api.dto.request.TransactionRequest;
import com.arthurisidoro.personal_finance_api.dto.response.DashboardResponse;
import com.arthurisidoro.personal_finance_api.dto.response.TransactionResponse;
import com.arthurisidoro.personal_finance_api.entity.Category;
import com.arthurisidoro.personal_finance_api.entity.Transaction;
import com.arthurisidoro.personal_finance_api.entity.TransactionType;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.BusinessRuleException;
import com.arthurisidoro.personal_finance_api.exception.ResourceNotFoundException;
import com.arthurisidoro.personal_finance_api.mapper.TransactionMapper;
import com.arthurisidoro.personal_finance_api.repository.CategoryRepository;
import com.arthurisidoro.personal_finance_api.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                               CategoryRepository categoryRepository,
                               TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
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
        return transactionMapper.toResponse(saved);
    }

    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionRepository.findByUserId(TempAuthConfig.TEMP_USER_ID, pageable)
                .map(transactionMapper::toResponse);
    }

    public TransactionResponse findById(Long id) {
        Transaction transaction = findOwnedOrThrow(id);
        return transactionMapper.toResponse(transaction);
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
        return transactionMapper.toResponse(updated);
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

    public Page<TransactionResponse> findWithFilters(
            LocalDate startDate, LocalDate endDate, String type, Long categoryId, Pageable pageable) {

        TransactionType parsedType = (type != null) ? parseType(type) : null;

        return transactionRepository.findWithFilters(
                TempAuthConfig.TEMP_USER_ID, startDate, endDate, parsedType, categoryId, pageable
        ).map(transactionMapper::toResponse);
    }

    public DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumByUserIdAndTypeAndDateRange(
                TempAuthConfig.TEMP_USER_ID, TransactionType.INCOME, startDate, endDate);

        BigDecimal totalExpense = transactionRepository.sumByUserIdAndTypeAndDateRange(
                TempAuthConfig.TEMP_USER_ID, TransactionType.EXPENSE, startDate, endDate);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new DashboardResponse(totalIncome, totalExpense, balance);
    }
}