package com.arthurisidoro.personal_finance_api.integration;

import com.arthurisidoro.personal_finance_api.dto.request.CategoryRequest;
import com.arthurisidoro.personal_finance_api.dto.request.LoginRequest;
import com.arthurisidoro.personal_finance_api.dto.request.RegisterRequest;
import com.arthurisidoro.personal_finance_api.dto.request.TransactionRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveExecutarFluxoCompletoDeRegistroLoginECriacao() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Arthur Integration");
        registerRequest.setEmail("integration@teste.com");
        registerRequest.setPassword("123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("integration@teste.com");
        loginRequest.setPassword("123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String token = jsonNode.get("token").asText();

        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());

        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setName("Alimentação");
        categoryRequest.setType("EXPENSE");

        MvcResult categoryResult = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alimentação"))
                .andReturn();

        JsonNode categoryJson = objectMapper.readTree(categoryResult.getResponse().getContentAsString());
        Long categoryId = categoryJson.get("id").asLong();

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setDescription("Almoço");
        transactionRequest.setAmount(new BigDecimal("32.90"));
        transactionRequest.setType("EXPENSE");
        transactionRequest.setDate(LocalDate.of(2026, 9, 13));
        transactionRequest.setCategoryId(categoryId);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Almoço"));

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Almoço"));
    }
}