package com.financial.mcp.core.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @JsonProperty("trace_id")
    private String traceId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("caller_id")
    private String callerId;

    @JsonProperty("tool_id")
    private String toolId;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params_hash")
    private String paramsHash;

    @JsonProperty("result_code")
    private String resultCode;

    @JsonProperty("latency_ms")
    private long latencyMs;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("dept")
    private String dept;
}
