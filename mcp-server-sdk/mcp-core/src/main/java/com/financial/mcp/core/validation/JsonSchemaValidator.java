package com.financial.mcp.core.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonSchemaValidator {
    private final ObjectMapper objectMapper;

    public void validate(Object params, JsonNode schema) {
        if (schema == null) {
            return;
        }

        JsonNode paramsNode = objectMapper.valueToTree(params);
        
        // Basic schema validation - check required fields
        JsonNode required = schema.get("required");
        if (required != null && required.isArray()) {
            for (JsonNode field : required) {
                String fieldName = field.asText();
                if (!paramsNode.has(fieldName)) {
                    throw new McpException(
                            McpErrorCode.INVALID_PARAMS,
                            "Missing required field: " + fieldName,
                            false
                    );
                }
            }
        }

        // Check field types if defined
        JsonNode properties = schema.get("properties");
        if (properties != null && properties.isObject()) {
            properties.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldSchema = entry.getValue();
                
                if (paramsNode.has(fieldName)) {
                    JsonNode fieldValue = paramsNode.get(fieldName);
                    String expectedType = fieldSchema.get("type").asText();
                    
                    if (!isValidType(fieldValue, expectedType)) {
                        throw new McpException(
                                McpErrorCode.INVALID_PARAMS,
                                "Field '" + fieldName + "' has invalid type. Expected: " + expectedType,
                                false
                        );
                    }
                }
            });
        }
    }

    private boolean isValidType(JsonNode value, String expectedType) {
        return switch (expectedType) {
            case "string" -> value.isTextual();
            case "number" -> value.isNumber();
            case "integer" -> value.isIntegralNumber();
            case "boolean" -> value.isBoolean();
            case "array" -> value.isArray();
            case "object" -> value.isObject();
            default -> true;
        };
    }
}
