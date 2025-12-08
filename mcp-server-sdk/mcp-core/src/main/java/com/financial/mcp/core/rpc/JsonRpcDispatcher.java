package com.financial.mcp.core.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.audit.AuditService;
import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
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
    private final ObjectMapper objectMapper;
    private final JsonRpcHandler handler;

    public JsonRpcResponse dispatch(JsonRpcRequest request) {
        long startTime = System.currentTimeMillis();
        String resultCode = "SUCCESS";
        String errorMessage = null;

        try {
            // 1. Validate JSON-RPC structure
            request.validate();

            // 2. Validate global kill switch
            killSwitchService.validateGlobalNotDisabled();

            // 3. Extract tool ID from method (format: "namespace.tool_id")
            String toolId = extractToolId(request.getMethod());

            // 4. Validate tool exists
            toolRegistryService.validateToolExists(toolId);

            // 5. Validate tool not disabled
            killSwitchService.validateToolNotDisabled(toolId);

            // 6. Validate authorization
            policyService.validatePolicy(request.getMeta(), toolId);

            // 7. Validate input schema
            var tool = toolRegistryService.getToolRegistry(toolId);
            schemaValidator.validate(request.getParams(), tool.getInputSchema());

            // 8. Execute handler
            Object result = handler.handle(request);

            // 9. Apply data masking
            var maskingPolicy = policyService.getDataMaskingPolicy(
                    request.getMeta().getUserId(),
                    toolId
            );
            Object maskedResult = maskingService.maskData(result, maskingPolicy);

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

    private String extractToolId(String method) {
        if (method == null || !method.contains(".")) {
            throw new IllegalArgumentException("Invalid method format. Expected: namespace.tool_id");
        }
        return method.split("\\.")[1];
    }
}
