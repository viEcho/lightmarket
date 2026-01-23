package com.market.business.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥（生产环境应从配置文件读取）
     */
    private static final String SECRET_KEY = "light-market-jwt-secret-key-2026-spring-boot-3";

    /**
     * Token过期时间：7天（毫秒）
     */
    private static final long TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @return JWT token
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 从token中解析用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证token是否有效
     *
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
