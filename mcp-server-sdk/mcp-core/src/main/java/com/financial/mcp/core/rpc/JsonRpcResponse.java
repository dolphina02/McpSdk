package com.financial.mcp.core.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    @JsonProperty("result")
    private Object result;

    @JsonProperty("error")
    private JsonRpcError error;

    @JsonProperty("id")
    private String id;

    public static JsonRpcResponse success(String id, Object result) {
        return JsonRpcResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }

    public static JsonRpcResponse error(String id, String code, String message, boolean retryable) {
        return JsonRpcResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(JsonRpcError.builder()
                        .code(code)
                        .message(message)
                        .retryable(retryable)
                        .build())
                .build();
    }
}
