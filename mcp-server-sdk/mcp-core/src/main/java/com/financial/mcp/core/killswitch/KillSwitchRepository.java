package com.financial.mcp.core.killswitch;

public interface KillSwitchRepository {
    KillSwitchStatus getToolStatus(String toolId);
    KillSwitchStatus getGlobalStatus();
    void setToolStatus(String toolId, boolean disabled, String reason);
    void setGlobalStatus(boolean disabled, String reason);
}
