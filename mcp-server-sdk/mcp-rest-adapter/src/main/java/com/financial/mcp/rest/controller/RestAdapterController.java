package com.financial.mcp.rest.controller;

import com.financial.mcp.core.rpc.JsonRpcDispatcher;
import com.financial.mcp.core.rpc.JsonRpcRequest;
import com.financial.mcp.core.rpc.JsonRpcResponse;
import com.financial.mcp.rest.converter.RestToJsonRpcConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestAdapterController {
    private final RestToJsonRpcConverter converter;
    private final JsonRpcDispatcher dispatcher;

    @PostMapping("/**")
    public ResponseEntity<Object> handleRestCall(
            @RequestBody Object params,
            HttpServletRequest request) {
        
        String path = request.getRequestURI().replace("/api/", "");
        String toolId = path.replace("/", ".");

        JsonRpcRequest jsonRpcRequest = converter.convertToJsonRpc(toolId, params, request);
        JsonRpcResponse response = dispatcher.dispatch(jsonRpcRequest);

        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response.getResult());
    }
}
