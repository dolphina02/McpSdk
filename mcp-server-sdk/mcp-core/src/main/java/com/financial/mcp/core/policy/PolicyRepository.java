package com.financial.mcp.core.policy;

public interface PolicyRepository {
    ToolPolicy findPolicyByUserAndTool(String userId, String toolId);
    
    /**
     * Find policy by user, tool, and version (composite key).
     * 
     * @param userId User ID
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return ToolPolicy or null if not found
     */
    ToolPolicy findPolicyByUserToolAndVersion(String userId, String toolId, String version);
    
    DataMaskingPolicy findMaskingPolicy(String userId, String toolId);
    
    /**
     * Find masking policy by user, tool, and version.
     * 
     * @param userId User ID
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return DataMaskingPolicy or null if not found
     */
    DataMaskingPolicy findMaskingPolicyByVersion(String userId, String toolId, String version);
    
    void savePolicy(ToolPolicy policy);
    void saveMaskingPolicy(DataMaskingPolicy policy);
}
