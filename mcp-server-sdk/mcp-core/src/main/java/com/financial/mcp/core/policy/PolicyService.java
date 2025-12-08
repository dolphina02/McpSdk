package com.financial.mcp.core.policy;

import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import com.financial.mcp.core.meta.McpMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository repository;

    public void validateAuthorization(String userId, String toolId) {
        ToolPolicy policy = repository.findPolicyByUserAndTool(userId, toolId);
        if (policy == null || !policy.isAllowed()) {
            throw new McpException(
                    McpErrorCode.POLICY_DENIED,
                    "User not authorized to access tool: " + toolId,
                    false
            );
        }
    }

    public DataMaskingPolicy getDataMaskingPolicy(String userId, String toolId) {
        return repository.findMaskingPolicy(userId, toolId);
    }

    public void validatePolicy(McpMeta meta, String toolId) {
        validateAuthorization(meta.getUserId(), toolId);
    }
}
