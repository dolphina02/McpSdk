package com.financial.mcp.core.error;

import lombok.Getter;

@Getter
public class McpException extends RuntimeException {
    private final String code;
    private final boolean retryable;

    public McpException(String code, String message, boolean retryable) {
        super(message);
        this.code = code;
        this.retryable = retryable;
    }

    public McpException(String code, String message) {
        this(code, message, false);
    }
}
