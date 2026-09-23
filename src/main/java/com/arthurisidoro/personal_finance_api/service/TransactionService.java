package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.TransactionRequest;
import com.arthurisidoro.personal_finance_api.dto.response.CategoryReportResponse;
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
import com.arthurisidoro.personal_finance_api.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    public TransactionService(TransactionRepository transactionRepository,
                               CategoryRepository categoryRepository,
                               TransactionMapper transactionMapper,
                               CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Category category = findOwnedCategoryOrThrow(request.getCategoryId());
        TransactionType type = parseType(request.getType());

        Transaction transaction = new Transaction();
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setType(type);
        transaction.setDate(request.getDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setCategory(category);
        transaction.setUser(currentUser);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    public Page<TransactionResponse> findAll(Pageable pageable) {
        Long userId = currentUserService.getCurrentUserId();
        return transactionRepository.findByUserId(userId, pageable)
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

    public Page<TransactionResponse> findWithFilters(
            LocalDate startDate, LocalDate endDate, String type, Long categoryId, Pageable pageable) {

        Long userId = currentUserService.getCurrentUserId();
        TransactionType parsedType = (type != null) ? parseType(type) : null;

        return transactionRepository.findWithFilters(
                userId, startDate, endDate, parsedType, categoryId, pageable
        ).map(transactionMapper::toResponse);
    }

    public DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate) {
        Long userId = currentUserService.getCurrentUserId();

        BigDecimal totalIncome = transactionRepository.sumByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, startDate, endDate);

        BigDecimal totalExpense = transactionRepository.sumByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, startDate, endDate);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new DashboardResponse(totalIncome, totalExpense, balance);
    }

    public List<CategoryReportResponse> getCategoryReport(LocalDate startDate, LocalDate endDate) {
        Long userId = currentUserService.getCurrentUserId();
        return transactionRepository.findExpensesGroupedByCategory(userId, startDate, endDate);
    }

    private Transaction findOwnedOrThrow(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
    }

    private Category findOwnedCategoryOrThrow(Long categoryId) {
        Long userId = currentUserService.getCurrentUserId();
        return categoryRepository.findByIdAndUserId(categoryId, userId)
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
}