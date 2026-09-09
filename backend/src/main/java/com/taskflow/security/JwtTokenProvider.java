package com.taskflow.security;

import com.taskflow.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 生成与解析（HS256）。
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 token，claims 中携带 uid / nick，subject 为用户名 */
    public String createToken(AuthUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.username())
                .claim("uid", user.id())
                .claim("nick", StringUtils.hasText(user.nickname()) ? user.nickname() : user.username())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(properties.expireHours()))))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** 解析 token（签名校验 + 过期校验由 jjwt 完成，异常抛给调用方处理） */
    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /** 剩余有效秒数（用于登录响应） */
    public long expiresInSeconds() {
        return Duration.ofHours(properties.expireHours()).toSeconds();
    }
}
