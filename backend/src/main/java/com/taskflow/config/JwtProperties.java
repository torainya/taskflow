package com.taskflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * JWT 相关配置（app.jwt.*），可用环境变量覆盖。
 *
 * @param secret       签名密钥，HS256 要求至少 32 字节
 * @param expireHours  过期小时数
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, @DefaultValue("24") Integer expireHours) {
}
