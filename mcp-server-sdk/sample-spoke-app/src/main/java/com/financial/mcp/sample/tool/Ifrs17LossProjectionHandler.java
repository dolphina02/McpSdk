package com.financial.mcp.sample.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.rpc.JsonRpcHandler;
import com.financial.mcp.core.rpc.JsonRpcRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Ifrs17LossProjectionHandler implements JsonRpcHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Object handle(JsonRpcRequest request) {
        if ("api.ifrs17.loss_projection".equals(request.getMethod())) {
            return handleLossProjection(request.getParams());
        }
        throw new IllegalArgumentException("Unknown method: " + request.getMethod());
    }

    private Object handleLossProjection(Object params) {
        Map<String, Object> paramsMap = objectMapper.convertValue(params, Map.class);
        
        Double portfolio = (Double) paramsMap.get("portfolio_value");
        Double lossRate = (Double) paramsMap.get("loss_rate");
        Integer years = (Integer) paramsMap.get("projection_years");

        Map<String, Object> result = new HashMap<>();
        result.put("portfolio_value", portfolio);
        result.put("loss_rate", lossRate);
        result.put("projection_years", years);
        
        // Simple projection calculation
        double projectedLoss = portfolio * lossRate * years;
        result.put("projected_loss", projectedLoss);
        result.put("remaining_value", portfolio - projectedLoss);
        result.put("confidence_level", 0.95);
        
        return result;
    }
}
