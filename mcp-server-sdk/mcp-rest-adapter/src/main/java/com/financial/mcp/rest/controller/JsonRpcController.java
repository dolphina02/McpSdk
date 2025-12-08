package com.financial.mcp.rest.controller;

import com.financial.mcp.core.rpc.JsonRpcDispatcher;
import com.financial.mcp.core.rpc.JsonRpcRequest;
import com.financial.mcp.core.rpc.JsonRpcResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp/rpc")
@RequiredArgsConstructor
public class JsonRpcController {
    private final JsonRpcDispatcher dispatcher;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> handleJsonRpc(@RequestBody JsonRpcRequest request) {
        JsonRpcResponse response = dispatcher.dispatch(request);
        return ResponseEntity.ok(response);
    }
}
