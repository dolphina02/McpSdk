package com.financial.mcp.core.registry;

public interface ToolRegistryRepository {
    ToolRegistry findByToolId(String toolId);
    
    /**
     * Find tool by tool ID and version (composite key).
     * 
     * @param toolId Tool ID (without version)
     * @param version Tool version
     * @return ToolRegistry or null if not found
     */
    ToolRegistry findByToolIdAndVersion(String toolId, String version);
    
    void save(ToolRegistry tool);
    void delete(String toolId);
}
