package com.financial.mcp.core.killswitch;

import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KillSwitchService {
    private final KillSwitchRepository repository;

    public void validateToolNotDisabled(String toolId) {
        KillSwitchStatus status = repository.getToolStatus(toolId);
        if (status != null && status.isDisabled()) {
            throw new McpException(
                    McpErrorCode.TOOL_DISABLED,
                    "Tool is disabled by kill switch: " + toolId,
                    false
            );
        }
    }

    public void validateGlobalNotDisabled() {
        KillSwitchStatus status = repository.getGlobalStatus();
        if (status != null && status.isDisabled()) {
            throw new McpException(
                    McpErrorCode.TOOL_DISABLED,
                    "MCP server is globally disabled",
                    false
            );
        }
    }

    public void disableTool(String toolId, String reason) {
        repository.setToolStatus(toolId, true, reason);
    }

    public void enableTool(String toolId) {
        repository.setToolStatus(toolId, false, null);
    }

    public void disableGlobal(String reason) {
        repository.setGlobalStatus(true, reason);
    }

    public void enableGlobal() {
        repository.setGlobalStatus(false, null);
    }

    public KillSwitchStatus getToolStatus(String toolId) {
        return repository.getToolStatus(toolId);
    }

    public KillSwitchStatus getGlobalStatus() {
        return repository.getGlobalStatus();
    }
}
