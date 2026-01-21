package com.market.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 调试控制器 - 用于排查问题
 */
@Slf4j
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Operation(summary = "调试接口 - 查看原始请求")
    @PostMapping("/test")
    public Map<String, Object> debugRequest(HttpServletRequest request) throws IOException {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取请求方法
        result.put("method", request.getMethod());

        // 2. 获取所有请求头
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        result.put("headers", headers);

        // 3. 获取请求参数
        Map<String, String[]> params = request.getParameterMap();
        result.put("parameters", params);

        // 4. 获取请求体（原始 JSON）
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String rawBody = sb.toString();
        result.put("rawBody", rawBody);

        // 5. 解析 JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = mapper.readValue(rawBody, Map.class);
            result.put("parsedJson", jsonMap);
        } catch (Exception e) {
            result.put("parseError", e.getMessage());
        }

        // 打印日志
        log.info("====== 调试信息 ======");
        log.info("原始请求体: {}", rawBody);
        log.info("========================");

        return result;
    }
}
