package com.financial.mcp.sample.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.registry.ToolRegistry;
import com.financial.mcp.core.registry.ToolRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SampleToolRegistry implements CommandLineRunner {
    private final ToolRegistryRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        // Register IFRS17 Loss Projection tool - Version 1
        String schemaV1 = """
                {
                  "type": "object",
                  "required": ["portfolio_value", "loss_rate", "projection_years"],
                  "properties": {
                    "portfolio_value": {"type": "number"},
                    "loss_rate": {"type": "number"},
                    "projection_years": {"type": "integer"}
                  }
                }
                """;

        JsonNode schemaNodeV1 = objectMapper.readTree(schemaV1);

        ToolRegistry toolV1 = ToolRegistry.builder()
                .toolId("loss_projection")
                .toolName("IFRS17 Loss Projection")
                .version("1.0.0")
                .status("ACTIVE")
                .inputSchema(schemaNodeV1)
                .description("Calculate IFRS17 loss projections for insurance portfolios (v1)")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        repository.save(toolV1);

        // Register IFRS17 Loss Projection tool - Version 2 (enhanced)
        String schemaV2 = """
                {
                  "type": "object",
                  "required": ["portfolio_value", "loss_rate", "projection_years", "confidence_level"],
                  "properties": {
                    "portfolio_value": {"type": "number"},
                    "loss_rate": {"type": "number"},
                    "projection_years": {"type": "integer"},
                    "confidence_level": {"type": "number", "minimum": 0, "maximum": 1},
                    "currency": {"type": "string", "default": "USD"}
                  }
                }
                """;

        JsonNode schemaNodeV2 = objectMapper.readTree(schemaV2);

        ToolRegistry toolV2 = ToolRegistry.builder()
                .toolId("loss_projection")
                .toolName("IFRS17 Loss Projection")
                .version("2.0.0")
                .status("ACTIVE")
                .inputSchema(schemaNodeV2)
                .description("Calculate IFRS17 loss projections for insurance portfolios (v2 - enhanced with confidence levels)")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        repository.save(toolV2);
    }
}
