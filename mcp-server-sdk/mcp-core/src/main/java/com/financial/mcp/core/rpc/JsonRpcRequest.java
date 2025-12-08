package com.financial.mcp.core.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financial.mcp.core.meta.McpMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonRpcRequest {
    @JsonProperty("jsonrpc")
    private String jsonrpc;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private Object params;

    @JsonProperty("id")
    private String id;

    @JsonProperty("meta")
    private McpMeta meta;

    public void validate() {
        if (!"2.0".equals(jsonrpc)) {
            throw new IllegalArgumentException("jsonrpc must be '2.0'");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method is required");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required (UUID v7)");
        }
        if (meta == null) {
            throw new IllegalArgumentException("meta is required");
        }
        meta.validate();
    }
}
