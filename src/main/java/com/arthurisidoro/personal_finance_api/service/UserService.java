package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.LoginRequest;
import com.arthurisidoro.personal_finance_api.dto.request.RegisterRequest;
import com.arthurisidoro.personal_finance_api.dto.request.UpdatePreferencesRequest;
import com.arthurisidoro.personal_finance_api.dto.response.LoginResponse;
import com.arthurisidoro.personal_finance_api.dto.response.UserPreferencesResponse;
import com.arthurisidoro.personal_finance_api.dto.response.UserResponse;
import com.arthurisidoro.personal_finance_api.entity.Currency;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.BusinessRuleException;
import com.arthurisidoro.personal_finance_api.exception.EmailAlreadyExistsException;
import com.arthurisidoro.personal_finance_api.mapper.UserMapper;
import com.arthurisidoro.personal_finance_api.repository.UserRepository;
import com.arthurisidoro.personal_finance_api.security.CurrentUserService;
import com.arthurisidoro.personal_finance_api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final List<String> SUPPORTED_LANGUAGES = List.of("pt-BR", "en-US");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        UserMapper userMapper, AuthenticationManager authenticationManager,
                        JwtService jwtService, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtService.generateToken(request.getEmail());
        return new LoginResponse(token);
    }

    public UserPreferencesResponse getPreferences() {
        User currentUser = currentUserService.getCurrentUser();
        return userMapper.toPreferencesResponse(currentUser);
    }

    @Transactional
    public UserPreferencesResponse updatePreferences(UpdatePreferencesRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        currentUser.setCurrency(parseCurrency(request.getCurrency()));
        currentUser.setLanguage(parseLanguage(request.getLanguage()));

        User updated = userRepository.save(currentUser);
        return userMapper.toPreferencesResponse(updated);
    }

    private Currency parseCurrency(String currency) {
        try {
            return Currency.valueOf(currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid currency. Use BRL, USD or EUR");
        }
    }

    private String parseLanguage(String language) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new BusinessRuleException("Invalid language. Use pt-BR or en-US");
        }
        return language;
    }
}