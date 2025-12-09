package com.financial.mcp.core.killswitch;

public interface KillSwitchRepository {
    KillSwitchStatus getToolStatus(String toolId);
    
    /**
     * Get kill switch status for tool version (composite key).
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return KillSwitchStatus or null if not found
     */
    KillSwitchStatus getToolVersionStatus(String toolId, String version);
    
    KillSwitchStatus getGlobalStatus();
    
    void setToolStatus(String toolId, boolean disabled, String reason);
    
    /**
     * Set kill switch status for tool version.
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @param disabled Whether to disable
     * @param reason Reason for change
     */
    void setToolVersionStatus(String toolId, String version, boolean disabled, String reason);
    
    void setGlobalStatus(boolean disabled, String reason);
}
