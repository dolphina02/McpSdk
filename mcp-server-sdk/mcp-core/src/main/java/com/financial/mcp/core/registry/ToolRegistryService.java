package com.financial.mcp.core.registry;

import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolRegistryService {
    private final ToolRegistryRepository repository;

    /**
     * Get tool registry by tool ID.
     * Supports both versioned (tool.v1) and unversioned (tool) lookups.
     * 
     * @param toolId Tool ID (may include version)
     * @return ToolRegistry
     * @throws McpException if tool not found
     */
    public ToolRegistry getToolRegistry(String toolId) {
        ToolRegistry tool = repository.findByToolId(toolId);
        if (tool == null) {
            throw new McpException(
                    McpErrorCode.TOOL_NOT_FOUND,
                    "Tool not found: " + toolId,
                    false
            );
        }
        return tool;
    }

    /**
     * Get tool registry by tool ID and version.
     * Uses composite key (tool_id + version).
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return ToolRegistry
     * @throws McpException if tool not found
     */
    public ToolRegistry getToolRegistryByVersion(String toolId, String version) {
        ToolRegistry tool = repository.findByToolIdAndVersion(toolId, version);
        if (tool == null) {
            throw new McpException(
                    McpErrorCode.TOOL_NOT_FOUND,
                    "Tool not found: " + toolId + " version: " + version,
                    false
            );
        }
        return tool;
    }

    public void validateToolExists(String toolId) {
        getToolRegistry(toolId);
    }

    public void validateToolActive(String toolId) {
        ToolRegistry tool = getToolRegistry(toolId);
        if (!tool.isActive()) {
            throw new McpException(
                    McpErrorCode.TOOL_DISABLED,
                    "Tool is disabled: " + toolId,
                    false
            );
        }
    }

    /**
     * Validate tool exists and is active by tool ID and version.
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @throws McpException if tool not found or disabled
     */
    public void validateToolActiveByVersion(String toolId, String version) {
        ToolRegistry tool = getToolRegistryByVersion(toolId, version);
        if (!tool.isActive()) {
            throw new McpException(
                    McpErrorCode.TOOL_DISABLED,
                    "Tool is disabled: " + toolId + " version: " + version,
                    false
            );
        }
    }
}
