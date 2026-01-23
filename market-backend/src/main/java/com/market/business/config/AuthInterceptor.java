package com.market.business.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.business.global.Result;
import com.market.business.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.time.Duration;

/**
 * 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_KEY_PREFIX = "auth:token:";
    private static final Duration TOKEN_EXPIRATION = Duration.ofDays(7);

    public AuthInterceptor(JwtUtil jwtUtil, RedissonClient redissonClient, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("AuthInterceptor checking: {} {}", method, requestURI);

        // 从请求头获取token
        String authHeader = request.getHeader(TOKEN_HEADER);

        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.warn("Missing or invalid Authorization header for: {}", requestURI);
            sendErrorResponse(response, 401, "未提供有效的认证令牌");
            return false;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        // 验证token格式
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid token format for: {}", requestURI);
            sendErrorResponse(response, 401, "无效的认证令牌");
            return false;
        }

        // 从token中解析用户ID
        Long tokenUserId = jwtUtil.getUserIdFromToken(token);
        if (tokenUserId == null) {
            log.warn("Failed to extract userId from token for: {}", requestURI);
            sendErrorResponse(response, 401, "无法从令牌中解析用户信息");
            return false;
        }

        // 检查Redis中的token
        String redisKey = TOKEN_KEY_PREFIX + tokenUserId;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        String storedToken = bucket.get();

        if (storedToken == null) {
            log.warn("Token not found in Redis for userId: {}, uri: {}", tokenUserId, requestURI);
            sendErrorResponse(response, 401, "令牌已过期，请重新登录");
            return false;
        }

        if (!storedToken.equals(token)) {
            log.warn("Token mismatch for userId: {}, uri: {}", tokenUserId, requestURI);
            // 删除Redis中的token
            bucket.delete();
            sendErrorResponse(response, 401, "令牌无效，请重新登录");
            return false;
        }

        // 检查请求参数中的userId是否与token中的userId一致
        String requestUserId = request.getParameter("userId");
        if (requestUserId != null) {
            try {
                Long requestUserIdLong = Long.parseLong(requestUserId);
                if (!requestUserIdLong.equals(tokenUserId)) {
                    log.error("Illegal request: userId mismatch! Token userId: {}, Request userId: {}, uri: {}",
                            tokenUserId, requestUserIdLong, requestURI);
                    // 删除Redis中的token
                    bucket.delete();
                    sendErrorResponse(response, 403, "非法请求：用户ID不匹配");
                    return false;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid userId parameter format: {}", requestUserId);
                sendErrorResponse(response, 400, "无效的用户ID格式");
                return false;
            }
        }

        // 将userId存储到请求属性中，供后续使用
        request.setAttribute("userId", tokenUserId);

        // 刷新token过期时间（每次请求都刷新，实现滑动过期）
        bucket.expire(TOKEN_EXPIRATION);

        return true;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.fail(message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
