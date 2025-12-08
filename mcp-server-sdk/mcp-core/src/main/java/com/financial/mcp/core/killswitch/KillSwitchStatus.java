package com.financial.mcp.core.killswitch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KillSwitchStatus {
    private String targetId; // tool_id or "GLOBAL"
    private boolean disabled;
    private String reason;
    private long disabledAt;
    private String disabledBy;
}
