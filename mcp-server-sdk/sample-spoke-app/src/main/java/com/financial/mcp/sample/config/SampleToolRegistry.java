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
        // Register IFRS17 Loss Projection tool
        String schemaJson = """
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

        JsonNode schema = objectMapper.readTree(schemaJson);

        ToolRegistry tool = ToolRegistry.builder()
                .toolId("ifrs17.loss_projection")
                .toolName("IFRS17 Loss Projection")
                .version("1.0.0")
                .status("ACTIVE")
                .inputSchema(schema)
                .description("Calculate IFRS17 loss projections for insurance portfolios")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        repository.save(tool);
    }
}
