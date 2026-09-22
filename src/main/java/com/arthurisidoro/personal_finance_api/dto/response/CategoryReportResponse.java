package com.arthurisidoro.personal_finance_api.dto.response;

import java.math.BigDecimal;

public class CategoryReportResponse {

    private Long categoryId;
    private String categoryName;
    private BigDecimal totalAmount;

    public CategoryReportResponse(Long categoryId, String categoryName, BigDecimal totalAmount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}