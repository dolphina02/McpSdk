package com.financial.mcp.sample.controller;

import com.financial.mcp.security.jwt.DevJwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 개발 환경용 JWT 토큰 생성 엔드포인트
 * 프로덕션에서는 사용하지 마세요!
 */
@Slf4j
@RestController
@RequestMapping("/dev/token")
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "true")
@RequiredArgsConstructor
public class DevTokenController {
    private final DevJwtTokenProvider tokenProvider;

    /**
     * 기본 테스트 토큰 생성
     * GET /dev/token
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTestToken() {
        try {
            String token = tokenProvider.generateTestToken();
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("user_id", "user@company.com");
            response.put("dept", "RISK");
            response.put("roles", new String[]{"ADMIN", "USER"});
            response.put("expires_in", 86400);
            response.put("message", "Development token - Valid for 24 hours");
            
            log.info("Generated dev token for user@company.com");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate token", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 커스텀 토큰 생성
     * POST /dev/token?user_id=xxx&dept=yyy&roles=ADMIN,USER
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateCustomToken(
            @RequestParam(defaultValue = "user@company.com") String user_id,
            @RequestParam(defaultValue = "RISK") String dept,
            @RequestParam(defaultValue = "ADMIN,USER") String roles) {
        try {
            String[] roleArray = roles.split(",");
            String token = tokenProvider.generateToken(user_id, dept, roleArray);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("user_id", user_id);
            response.put("dept", dept);
            response.put("roles", roleArray);
            response.put("expires_in", 86400);
            response.put("message", "Development token - Valid for 24 hours");
            
            log.info("Generated dev token for user: {}, dept: {}", user_id, dept);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate token", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 사용 예제 및 가이드
     * GET /dev/token/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("message", "Development JWT Token Generator");
        help.put("endpoints", Map.of(
                "GET /dev/token", "Generate default test token",
                "POST /dev/token", "Generate custom token with parameters",
                "GET /dev/token/help", "Show this help message"
        ));
        help.put("parameters", Map.of(
                "user_id", "User identifier (default: user@company.com)",
                "dept", "Department (default: RISK)",
                "roles", "Comma-separated roles (default: ADMIN,USER)"
        ));
        help.put("examples", Map.of(
                "default_token", "curl http://localhost:8080/dev/token",
                "custom_token", "curl -X POST 'http://localhost:8080/dev/token?user_id=alice@company.com&dept=COMPLIANCE&roles=USER'",
                "usage_in_request", "curl -H 'Authorization: Bearer <token>' http://localhost:8080/mcp/rpc"
        ));
        help.put("warning", "⚠️  This endpoint is ONLY for development! Disable in production by setting mcp.security.dev-mode=false");
        
        return ResponseEntity.ok(help);
    }
}
