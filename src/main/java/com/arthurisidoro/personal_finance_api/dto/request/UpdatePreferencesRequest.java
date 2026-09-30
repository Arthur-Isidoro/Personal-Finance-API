package com.arthurisidoro.personal_finance_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdatePreferencesRequest {

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Language is required")
    private String language;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}