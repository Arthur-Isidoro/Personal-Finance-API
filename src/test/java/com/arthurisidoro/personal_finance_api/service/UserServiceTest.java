package com.arthurisidoro.personal_finance_api.service;

import com.arthurisidoro.personal_finance_api.dto.request.RegisterRequest;
import com.arthurisidoro.personal_finance_api.dto.response.UserResponse;
import com.arthurisidoro.personal_finance_api.entity.User;
import com.arthurisidoro.personal_finance_api.exception.EmailAlreadyExistsException;
import com.arthurisidoro.personal_finance_api.mapper.UserMapper;
import com.arthurisidoro.personal_finance_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private com.arthurisidoro.personal_finance_api.security.JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void deveCadastrarUsuarioComSucesso() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Arthur Isidoro");
        request.setEmail("arthur@teste.com");
        request.setPassword("123456");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Arthur Isidoro");
        savedUser.setEmail("arthur@teste.com");
        org.springframework.test.util.ReflectionTestUtils.setField(savedUser, "createdAt", LocalDateTime.now());

        UserResponse expectedResponse = new UserResponse(
                1L, "Arthur Isidoro", "arthur@teste.com", savedUser.getCreatedAt());

        when(userRepository.existsByEmail("arthur@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("senha-hasheada");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse response = userService.register(request);

        assertThat(response.getEmail()).isEqualTo("arthur@teste.com");
        assertThat(response.getName()).isEqualTo("Arthur Isidoro");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Arthur Isidoro");
        request.setEmail("arthur@teste.com");
        request.setPassword("123456");

        when(userRepository.existsByEmail("arthur@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}