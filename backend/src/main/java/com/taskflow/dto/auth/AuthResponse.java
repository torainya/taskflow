package com.taskflow.dto.auth;

/**
 * 登录/注册成功后的响应：access token + 用户摘要。
 */
public record AuthResponse(String token, String tokenType, long expiresIn, UserBrief user) {

    public record UserBrief(Long id, String username, String nickname) {
    }
}
