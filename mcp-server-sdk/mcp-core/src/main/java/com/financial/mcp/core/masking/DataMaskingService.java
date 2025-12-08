package com.financial.mcp.core.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.financial.mcp.core.policy.DataMaskingPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataMaskingService {
    private final ObjectMapper objectMapper;

    public Object maskData(Object data, DataMaskingPolicy policy) {
        if (policy == null || policy.getColumnMasks() == null || policy.getColumnMasks().isEmpty()) {
            return data;
        }

        JsonNode node = objectMapper.valueToTree(data);
        if (!node.isObject()) {
            return data;
        }

        ObjectNode masked = (ObjectNode) node;
        Map<String, String> masks = policy.getColumnMasks();

        masks.forEach((column, maskType) -> {
            if (masked.has(column)) {
                JsonNode value = masked.get(column);
                masked.put(column, applyMask(value.asText(), maskType));
            }
        });

        return objectMapper.convertValue(masked, Object.class);
    }

    private String applyMask(String value, String maskType) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (maskType) {
            case "HASH" -> hashValue(value);
            case "REDACT" -> "***REDACTED***";
            case "PARTIAL" -> partialMask(value);
            default -> value;
        };
    }

    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.substring(0, 16);
        } catch (Exception e) {
            return "***ERROR***";
        }
    }

    private String partialMask(String value) {
        if (value.length() <= 4) {
            return "*".repeat(value.length());
        }
        int visibleChars = Math.max(2, value.length() / 4);
        return value.substring(0, visibleChars) + "*".repeat(value.length() - visibleChars);
    }
}
