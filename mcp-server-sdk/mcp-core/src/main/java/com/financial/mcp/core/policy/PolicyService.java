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

    /**
     * Validate authorization for tool with specific version.
     * Checks (user_id, tool_id, version) tuple.
     * 
     * @param userId User ID
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @throws McpException if not authorized
     */
    public void validateAuthorizationByVersion(String userId, String toolId, String version) {
        ToolPolicy policy = repository.findPolicyByUserToolAndVersion(userId, toolId, version);
        if (policy == null || !policy.isAllowed()) {
            throw new McpException(
                    McpErrorCode.POLICY_DENIED,
                    "User not authorized to access tool: " + toolId + " version: " + version,
                    false
            );
        }
    }

    public DataMaskingPolicy getDataMaskingPolicy(String userId, String toolId) {
        return repository.findMaskingPolicy(userId, toolId);
    }

    /**
     * Get data masking policy for tool with specific version.
     * 
     * @param userId User ID
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return DataMaskingPolicy or null if not found
     */
    public DataMaskingPolicy getDataMaskingPolicyByVersion(String userId, String toolId, String version) {
        return repository.findMaskingPolicyByVersion(userId, toolId, version);
    }

    public void validatePolicy(McpMeta meta, String toolId) {
        validateAuthorization(meta.getUserId(), toolId);
    }

    /**
     * Validate policy for tool with specific version.
     * 
     * @param meta MCP metadata
     * @param toolId Tool ID (without version)
     * @param version Tool version
     */
    public void validatePolicyByVersion(McpMeta meta, String toolId, String version) {
        validateAuthorizationByVersion(meta.getUserId(), toolId, version);
    }
}
