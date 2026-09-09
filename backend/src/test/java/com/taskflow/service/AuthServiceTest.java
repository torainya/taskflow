package com.taskflow.service;

import com.taskflow.domain.User;
import com.taskflow.dto.auth.AuthResponse;
import com.taskflow.dto.auth.LoginRequest;
import com.taskflow.dto.auth.RegisterRequest;
import com.taskflow.exception.BizException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userMapper, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void register_encodesPassword_andIssuesToken() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("Passw0rd")).thenReturn("$2a$hash");
        when(jwtTokenProvider.createToken(any())).thenReturn("jwt-token");
        when(jwtTokenProvider.expiresInSeconds()).thenReturn(86400L);

        AuthResponse response = authService.register(new RegisterRequest("neo", "Passw0rd", "Neo"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().username()).isEqualTo("neo");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$hash");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void register_duplicateUsername_throwsConflict() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("neo", "Passw0rd", null)))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", "USERNAME_TAKEN");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = new User();
        user.setId(1L);
        user.setUsername("neo");
        user.setPassword("$2a$correct-hash");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$correct-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("neo", "wrong")))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", "BAD_CREDENTIALS");
    }

    @Test
    void login_success_issuesToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("neo");
        user.setNickname("Neo");
        user.setPassword("$2a$correct-hash");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("Passw0rd", "$2a$correct-hash")).thenReturn(true);
        when(jwtTokenProvider.createToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("neo", "Passw0rd"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().nickname()).isEqualTo("Neo");
    }
}
