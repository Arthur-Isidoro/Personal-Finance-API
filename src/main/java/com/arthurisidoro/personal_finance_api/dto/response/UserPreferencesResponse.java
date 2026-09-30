package com.arthurisidoro.personal_finance_api.dto.response;

public class UserPreferencesResponse {

    private String currency;
    private String language;

    public UserPreferencesResponse(String currency, String language) {
        this.currency = currency;
        this.language = language;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLanguage() {
        return language;
    }
}
