package com.taskflow.security;

/**
 * 已认证用户的轻量身份信息（从 JWT 解析而来，不查库）。
 */
public record AuthUser(Long id, String username, String nickname) {
}
