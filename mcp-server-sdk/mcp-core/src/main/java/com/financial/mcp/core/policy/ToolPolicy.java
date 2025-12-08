package com.financial.mcp.core.policy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolPolicy {
    private String userId;
    private String toolId;
    private boolean allowed;
    private String dataLevel; // PUBLIC, INTERNAL, CONFIDENTIAL
    private long createdAt;
    private long updatedAt;
}
