package com.taskflow.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.taskflow.domain.User;
import com.taskflow.dto.auth.AuthResponse;
import com.taskflow.dto.auth.LoginRequest;
import com.taskflow.dto.auth.RegisterRequest;
import com.taskflow.exception.BizException;
import com.taskflow.mapper.UserMapper;
import com.taskflow.security.AuthUser;
import com.taskflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 注册 / 登录。出于 demo 的简洁性，直接校验密码后签发 JWT，
 * 不引入 AuthenticationManager + UserDetailsService 的完整链路（进阶可自行扩展）。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.username())) > 0) {
            throw BizException.conflict("USERNAME_TAKEN", "用户名已被占用");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : request.username());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册兜底（唯一索引兜底，正常不会走到）
            throw BizException.conflict("USERNAME_TAKEN", "用户名已被占用");
        }
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            // 统一提示，避免暴露「用户是否存在」
            throw BizException.unauthorized("BAD_CREDENTIALS", "用户名或密码错误");
        }
        return issueToken(user);
    }

    private AuthResponse issueToken(User user) {
        AuthUser principal = new AuthUser(user.getId(), user.getUsername(), user.getNickname());
        String token = jwtTokenProvider.createToken(principal);
        return new AuthResponse(
                token,
                "Bearer",
                jwtTokenProvider.expiresInSeconds(),
                new AuthResponse.UserBrief(user.getId(), user.getUsername(), user.getNickname()));
    }
}
