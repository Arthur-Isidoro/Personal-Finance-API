package com.arthurisidoro.personal_finance_api.dto.response;

import java.time.LocalDateTime;

public class CategoryResponse {

    private Long id;
    private String name;
    private String type;
    private LocalDateTime createdAt;

    public CategoryResponse(Long id, String name, String type, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}