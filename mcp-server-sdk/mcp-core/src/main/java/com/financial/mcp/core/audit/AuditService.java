package com.financial.mcp.core.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.meta.McpMeta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository repository;
    private final ObjectMapper objectMapper;

    @Async
    public void logCall(McpMeta meta, String toolId, String method, Object params,
                        String resultCode, long latencyMs, String errorMessage) {
        try {
            String paramsHash = hashParams(params);
            
            AuditLog log = AuditLog.builder()
                    .traceId(meta.getTraceId())
                    .userId(meta.getUserId())
                    .callerId(meta.getCallerId())
                    .toolId(toolId)
                    .method(method)
                    .paramsHash(paramsHash)
                    .resultCode(resultCode)
                    .latencyMs(latencyMs)
                    .timestamp(System.currentTimeMillis())
                    .errorMessage(errorMessage)
                    .dept(meta.getDept())
                    .build();

            repository.save(log);
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        }
    }

    private String hashParams(Object params) {
        try {
            String paramsJson = objectMapper.writeValueAsString(params);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(paramsJson.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
