package com.financial.mcp.core.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpMeta {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("caller_id")
    private String callerId;

    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("tx_id")
    private String txId;

    @JsonProperty("dept")
    private String dept;

    public void validate() {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("meta.user_id is required");
        }
        if (callerId == null || callerId.isBlank()) {
            throw new IllegalArgumentException("meta.caller_id is required");
        }
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("meta.trace_id is required");
        }
        if (txId == null || txId.isBlank()) {
            throw new IllegalArgumentException("meta.tx_id is required");
        }
        if (dept == null || dept.isBlank()) {
            throw new IllegalArgumentException("meta.dept is required");
        }
    }
}
