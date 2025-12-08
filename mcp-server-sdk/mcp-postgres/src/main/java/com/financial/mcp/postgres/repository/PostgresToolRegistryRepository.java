package com.financial.mcp.postgres.repository;

import com.financial.mcp.core.registry.ToolRegistry;
import com.financial.mcp.core.registry.ToolRegistryRepository;
import com.financial.mcp.postgres.entity.ToolRegistryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresToolRegistryRepository implements ToolRegistryRepository {
    private final ToolRegistryJpaRepository jpaRepository;

    @Override
    public ToolRegistry findByToolId(String toolId) {
        ToolRegistryEntity entity = jpaRepository.findByToolId(toolId);
        if (entity == null) {
            return null;
        }
        return mapToRegistry(entity);
    }

    @Override
    public void save(ToolRegistry tool) {
        ToolRegistryEntity entity = ToolRegistryEntity.builder()
                .toolId(tool.getToolId())
                .toolName(tool.getToolName())
                .version(tool.getVersion())
                .status(tool.getStatus())
                .inputSchema(tool.getInputSchema())
                .description(tool.getDescription())
                .createdAt(tool.getCreatedAt())
                .updatedAt(System.currentTimeMillis())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public void delete(String toolId) {
        ToolRegistryEntity entity = jpaRepository.findByToolId(toolId);
        if (entity != null) {
            jpaRepository.delete(entity);
        }
    }

    private ToolRegistry mapToRegistry(ToolRegistryEntity entity) {
        return ToolRegistry.builder()
                .toolId(entity.getToolId())
                .toolName(entity.getToolName())
                .version(entity.getVersion())
                .status(entity.getStatus())
                .inputSchema(entity.getInputSchema())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
