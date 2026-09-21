package com.arthurisidoro.personal_finance_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String type;
    private LocalDate date;
    private String paymentMethod;
    private CategorySummary category;

    public TransactionResponse(Long id, String description, BigDecimal amount, String type,
                               LocalDate date, String paymentMethod, CategorySummary category) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public CategorySummary getCategory() {
        return category;
    }

    public static class CategorySummary {
        private Long id;
        private String name;

        public CategorySummary(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}