package com.financial.mcp.core.rpc;

public interface JsonRpcHandler {
    Object handle(JsonRpcRequest request);
}
