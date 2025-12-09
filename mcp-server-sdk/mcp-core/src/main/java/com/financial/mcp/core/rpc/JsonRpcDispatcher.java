package com.financial.mcp.core.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.audit.AuditService;
import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import com.financial.mcp.core.idempotency.IdempotencyService;
import com.financial.mcp.core.killswitch.KillSwitchService;
import com.financial.mcp.core.masking.DataMaskingService;
import com.financial.mcp.core.policy.PolicyService;
import com.financial.mcp.core.registry.ToolRegistryService;
import com.financial.mcp.core.validation.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonRpcDispatcher {
    private final ToolRegistryService toolRegistryService;
    private final PolicyService policyService;
    private final KillSwitchService killSwitchService;
    private final JsonSchemaValidator schemaValidator;
    private final DataMaskingService maskingService;
    private final AuditService auditService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    private final JsonRpcHandler handler;

    public JsonRpcResponse dispatch(JsonRpcRequest request) {
        long startTime = System.currentTimeMillis();
        String resultCode = "SUCCESS";
        String errorMessage = null;

        try {
            // 1. Validate JSON-RPC structure
            request.validate();

            // 2. Check idempotency (BEFORE any handler execution)
            idempotencyService.checkAndMarkProcessing(request.getMeta().getTxId());

            // 3. Validate global kill switch
            killSwitchService.validateGlobalNotDisabled();

            // 4. Extract tool ID and version from method
            String toolId = extractToolId(request.getMethod());
            String version = extractVersion(request.getMethod());

            // 5. Validate tool exists (with version if provided)
            var tool = (version != null) 
                    ? toolRegistryService.getToolRegistryByVersion(toolId, version)
                    : toolRegistryService.getToolRegistry(toolId);

            // 6. Validate tool not disabled (with version if provided)
            if (version != null) {
                killSwitchService.validateToolNotDisabledByVersion(toolId, version);
            } else {
                killSwitchService.validateToolNotDisabled(toolId);
            }

            // 7. Validate authorization (with version if provided)
            if (version != null) {
                policyService.validateAuthorizationByVersion(request.getMeta().getUserId(), toolId, version);
            } else {
                policyService.validatePolicy(request.getMeta(), toolId);
            }

            // 8. Validate input schema
            schemaValidator.validate(request.getParams(), tool.getInputSchema());

            // 9. Execute handler
            Object result = handler.handle(request);

            // 10. Apply data masking (with version if provided)
            var maskingPolicy = (version != null)
                    ? policyService.getDataMaskingPolicyByVersion(request.getMeta().getUserId(), toolId, version)
                    : policyService.getDataMaskingPolicy(request.getMeta().getUserId(), toolId);
            Object maskedResult = maskingService.maskData(result, maskingPolicy);

            // 11. Mark transaction as completed
            idempotencyService.markCompleted(request.getMeta().getTxId());

            long latency = System.currentTimeMillis() - startTime;
            auditService.logCall(
                    request.getMeta(),
                    toolId,
                    request.getMethod(),
                    request.getParams(),
                    resultCode,
                    latency,
                    null
            );

            return JsonRpcResponse.success(request.getId(), maskedResult);

        } catch (McpException e) {
            resultCode = e.getCode();
            errorMessage = e.getMessage();
            long latency = System.currentTimeMillis() - startTime;
            
            // Mark transaction as failed if it was idempotency error
            if (McpErrorCode.DUPLICATE_TX.equals(e.getCode())) {
                try {
                    idempotencyService.markFailed(request.getMeta().getTxId());
                } catch (Exception ignored) {
                }
            }
            
            try {
                String toolId = extractToolId(request.getMethod());
                auditService.logCall(
                        request.getMeta(),
                        toolId,
                        request.getMethod(),
                        request.getParams(),
                        resultCode,
                        latency,
                        errorMessage
                );
            } catch (Exception ignored) {
            }

            return JsonRpcResponse.error(request.getId(), e.getCode(), e.getMessage(), e.isRetryable());

        } catch (IllegalArgumentException e) {
            resultCode = McpErrorCode.INVALID_PARAMS;
            errorMessage = e.getMessage();
            long latency = System.currentTimeMillis() - startTime;
            
            try {
                auditService.logCall(
                        request.getMeta(),
                        "UNKNOWN",
                        request.getMethod(),
                        request.getParams(),
                        resultCode,
                        latency,
                        errorMessage
                );
            } catch (Exception ignored) {
            }

            return JsonRpcResponse.error(request.getId(), McpErrorCode.INVALID_PARAMS, e.getMessage(), false);

        } catch (Exception e) {
            resultCode = McpErrorCode.MCP_INTERNAL_ERROR;
            errorMessage = e.getMessage();
            long latency = System.currentTimeMillis() - startTime;
            
            log.error("Unexpected error in JSON-RPC dispatch", e);
            try {
                auditService.logCall(
                        request.getMeta(),
                        "UNKNOWN",
                        request.getMethod(),
                        request.getParams(),
                        resultCode,
                        latency,
                        errorMessage
                );
            } catch (Exception ignored) {
            }

            return JsonRpcResponse.error(request.getId(), McpErrorCode.MCP_INTERNAL_ERROR, "Internal server error", false);
        }
    }

    /**
     * Extract tool ID from method name.
     * Supports both versioned and unversioned formats:
     * - Unversioned: namespace.tool_id
     * - Versioned: namespace.v1.tool_id or namespace.tool_id.v1
     * 
     * @param method Method name
     * @return Tool ID (without version)
     */
    private String extractToolId(String method) {
        if (method == null || !method.contains(".")) {
            throw new IllegalArgumentException("Invalid method format. Expected: namespace.tool_id or namespace.v1.tool_id");
        }
        String[] parts = method.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid method format. Expected: namespace.tool_id");
        }
        // Return second part (tool_id without version)
        return parts[1];
    }

    /**
     * Extract version from method name if present.
     * Supports versioned format: namespace.v1.tool_id
     * 
     * @param method Method name
     * @return Version string or null if not versioned
     */
    private String extractVersion(String method) {
        if (method == null || !method.contains(".")) {
            return null;
        }
        String[] parts = method.split("\\.");
        if (parts.length >= 3 && parts[1].startsWith("v")) {
            // Format: namespace.v1.tool_id
            return parts[1];
        }
        return null;
    }
}
