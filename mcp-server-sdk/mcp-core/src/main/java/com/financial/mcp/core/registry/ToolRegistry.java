package com.financial.mcp.core.registry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolRegistry {
    private String toolId;
    private String toolName;
    private String version;
    private String status; // ACTIVE, DISABLED
    private JsonNode inputSchema;
    private String description;
    private long createdAt;
    private long updatedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
