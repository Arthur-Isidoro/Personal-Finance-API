package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.LoginRequest;
import com.arthurisidoro.personal_finance_api.dto.request.RegisterRequest;
import com.arthurisidoro.personal_finance_api.dto.response.LoginResponse;
import com.arthurisidoro.personal_finance_api.dto.response.UserResponse;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.EmailAlreadyExistsException;
import com.arthurisidoro.personal_finance_api.mapper.UserMapper;
import com.arthurisidoro.personal_finance_api.repository.UserRepository;
import com.arthurisidoro.personal_finance_api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        UserMapper userMapper, AuthenticationManager authenticationManager,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
}