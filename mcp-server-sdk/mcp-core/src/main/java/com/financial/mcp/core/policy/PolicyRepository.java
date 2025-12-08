package com.financial.mcp.core.policy;

public interface PolicyRepository {
    ToolPolicy findPolicyByUserAndTool(String userId, String toolId);
    DataMaskingPolicy findMaskingPolicy(String userId, String toolId);
    void savePolicy(ToolPolicy policy);
    void saveMaskingPolicy(DataMaskingPolicy policy);
}
