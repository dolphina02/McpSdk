package com.financial.mcp.core.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolRegistryServiceVersionTest {
    @Mock
    private ToolRegistryRepository repository;

    private ToolRegistryService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = new ToolRegistryService(repository);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetToolRegistryByVersion_Success() throws Exception {
        String toolId = "loss_projection";
        String version = "1.0.0";
        
        JsonNode schema = objectMapper.readTree("{\"type\": \"object\"}");
        ToolRegistry tool = ToolRegistry.builder()
                .toolId(toolId)
                .toolName("Loss Projection")
                .version(version)
                .status("ACTIVE")
                .inputSchema(schema)
                .description("Test tool")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        when(repository.findByToolIdAndVersion(toolId, version)).thenReturn(tool);

        ToolRegistry result = service.getToolRegistryByVersion(toolId, version);

        assertNotNull(result);
        assertEquals(toolId, result.getToolId());
        assertEquals(version, result.getVersion());
        verify(repository).findByToolIdAndVersion(toolId, version);
    }

    @Test
    void testGetToolRegistryByVersion_NotFound() {
        String toolId = "loss_projection";
        String version = "1.0.0";

        when(repository.findByToolIdAndVersion(toolId, version)).thenReturn(null);

        McpException exception = assertThrows(McpException.class, () -> {
            service.getToolRegistryByVersion(toolId, version);
        });

        assertEquals(McpErrorCode.TOOL_NOT_FOUND, exception.getCode());
        assertTrue(exception.getMessage().contains(toolId));
        assertTrue(exception.getMessage().contains(version));
    }

    @Test
    void testValidateToolActiveByVersion_Success() throws Exception {
        String toolId = "loss_projection";
        String version = "2.0.0";
        
        JsonNode schema = objectMapper.readTree("{\"type\": \"object\"}");
        ToolRegistry tool = ToolRegistry.builder()
                .toolId(toolId)
                .toolName("Loss Projection")
                .version(version)
                .status("ACTIVE")
                .inputSchema(schema)
                .description("Test tool")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        when(repository.findByToolIdAndVersion(toolId, version)).thenReturn(tool);

        // Should not throw
        service.validateToolActiveByVersion(toolId, version);

        verify(repository).findByToolIdAndVersion(toolId, version);
    }

    @Test
    void testValidateToolActiveByVersion_Disabled() throws Exception {
        String toolId = "loss_projection";
        String version = "1.0.0";
        
        JsonNode schema = objectMapper.readTree("{\"type\": \"object\"}");
        ToolRegistry tool = ToolRegistry.builder()
                .toolId(toolId)
                .toolName("Loss Projection")
                .version(version)
                .status("DISABLED")
                .inputSchema(schema)
                .description("Test tool")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        when(repository.findByToolIdAndVersion(toolId, version)).thenReturn(tool);

        McpException exception = assertThrows(McpException.class, () -> {
            service.validateToolActiveByVersion(toolId, version);
        });

        assertEquals(McpErrorCode.TOOL_DISABLED, exception.getCode());
        assertTrue(exception.getMessage().contains(toolId));
        assertTrue(exception.getMessage().contains(version));
    }
}
