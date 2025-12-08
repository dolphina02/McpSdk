package com.financial.mcp.rest.converter;

import com.financial.mcp.core.meta.McpMeta;
import com.financial.mcp.core.rpc.JsonRpcRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestToJsonRpcConverter {

    public JsonRpcRequest convertToJsonRpc(String toolId, Object params, HttpServletRequest httpRequest) {
        String userId = extractUserId(httpRequest);
        String callerId = extractCallerId(httpRequest);
        String traceId = extractTraceId(httpRequest);
        String dept = extractDept(httpRequest);

        McpMeta meta = McpMeta.builder()
                .userId(userId)
                .callerId(callerId)
                .traceId(traceId)
                .dept(dept)
                .build();

        return JsonRpcRequest.builder()
                .jsonrpc("2.0")
                .method("api." + toolId)
                .params(params)
                .id(UUID.randomUUID().toString())
                .meta(meta)
                .build();
    }

    private String extractUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            // In production, extract from JWT token
            return "user_from_jwt";
        }
        return request.getHeader("X-User-Id");
    }

    private String extractCallerId(HttpServletRequest request) {
        return request.getHeader("X-Client-Id");
    }

    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    private String extractDept(HttpServletRequest request) {
        return request.getHeader("X-Dept");
    }
}
