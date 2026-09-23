package com.arthurisidoro.personal_finance_api.controller;

import com.arthurisidoro.personal_finance_api.config.SecurityConfig;
import com.arthurisidoro.personal_finance_api.dto.request.RegisterRequest;
import com.arthurisidoro.personal_finance_api.dto.response.UserResponse;
import com.arthurisidoro.personal_finance_api.exception.EmailAlreadyExistsException;
import com.arthurisidoro.personal_finance_api.security.JwtAuthFilter;
import com.arthurisidoro.personal_finance_api.security.JwtService;
import com.arthurisidoro.personal_finance_api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doAnswer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void deveRetornar201AoCadastrarComSucesso() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Arthur Isidoro");
        request.setEmail("arthur@teste.com");
        request.setPassword("123456");

        UserResponse response = new UserResponse(1L, "Arthur Isidoro", "arthur@teste.com", LocalDateTime.now());

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("arthur@teste.com"))
                .andExpect(jsonPath("$.name").value("Arthur Isidoro"));
    }

    @Test
    void deveRetornar409QuandoEmailJaExiste() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Arthur Isidoro");
        request.setEmail("arthur@teste.com");
        request.setPassword("123456");

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("arthur@teste.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoEmailInvalido() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Arthur Isidoro");
        request.setEmail("email-invalido");
        request.setPassword("123456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}