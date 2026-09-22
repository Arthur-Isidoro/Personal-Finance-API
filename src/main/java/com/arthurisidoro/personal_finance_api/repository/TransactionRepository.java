package com.arthurisidoro.personal_finance_api.repository;

import com.arthurisidoro.personal_finance_api.entity.Transaction;
import com.arthurisidoro.personal_finance_api.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
            AND (:startDate IS NULL OR t.date >= :startDate)
            AND (:endDate IS NULL OR t.date <= :endDate)
            AND (:type IS NULL OR t.type = :type)
            AND (:categoryId IS NULL OR t.category.id = :categoryId)
            """)
    Page<Transaction> findWithFilters(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query("""
                SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
                WHERE t.user.id = :userId
                AND t.type = :type
                AND (:startDate IS NULL OR t.date >= :startDate)
                AND (:endDate IS NULL OR t.date <= :endDate)
                """)
        BigDecimal sumByUserIdAndTypeAndDateRange(
                @Param("userId") Long userId,
                @Param("type") TransactionType type,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate
);
}