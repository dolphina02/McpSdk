package com.financial.mcp.core.registry;

public interface ToolRegistryRepository {
    ToolRegistry findByToolId(String toolId);
    void save(ToolRegistry tool);
    void delete(String toolId);
}
