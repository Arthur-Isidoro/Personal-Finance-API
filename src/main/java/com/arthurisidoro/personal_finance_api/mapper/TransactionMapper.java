package com.arthurisidoro.personal_finance_api.mapper;

import com.arthurisidoro.personal_finance_api.dto.response.TransactionResponse;
import com.arthurisidoro.personal_finance_api.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
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