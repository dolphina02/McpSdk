package com.financial.mcp.core.registry;

import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolRegistryService {
    private final ToolRegistryRepository repository;

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
}
