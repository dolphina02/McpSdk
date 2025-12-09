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

    /**
     * Validate tool with specific version is not disabled.
     * Checks both tool-level and tool+version-level kill switches.
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @throws McpException if tool or version is disabled
     */
    public void validateToolNotDisabledByVersion(String toolId, String version) {
        // Check tool-level disable
        KillSwitchStatus toolStatus = repository.getToolStatus(toolId);
        if (toolStatus != null && toolStatus.isDisabled()) {
            throw new McpException(
                    McpErrorCode.TOOL_DISABLED,
                    "Tool is disabled by kill switch: " + toolId,
                    false
            );
        }

        // Check tool+version-level disable
        KillSwitchStatus versionStatus = repository.getToolVersionStatus(toolId, version);
        if (versionStatus != null && versionStatus.isDisabled()) {
            throw new McpException(
                    McpErrorCode.TOOL_DISABLED,
                    "Tool version is disabled by kill switch: " + toolId + " version: " + version,
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

    /**
     * Disable specific tool version.
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @param reason Reason for disabling
     */
    public void disableToolVersion(String toolId, String version, String reason) {
        repository.setToolVersionStatus(toolId, version, true, reason);
    }

    public void enableTool(String toolId) {
        repository.setToolStatus(toolId, false, null);
    }

    /**
     * Enable specific tool version.
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     */
    public void enableToolVersion(String toolId, String version) {
        repository.setToolVersionStatus(toolId, version, false, null);
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

    /**
     * Get kill switch status for tool version.
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return KillSwitchStatus or null if not found
     */
    public KillSwitchStatus getToolVersionStatus(String toolId, String version) {
        return repository.getToolVersionStatus(toolId, version);
    }

    public KillSwitchStatus getGlobalStatus() {
        return repository.getGlobalStatus();
    }
}
