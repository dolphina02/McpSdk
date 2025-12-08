package com.financial.mcp.postgres.repository;

import com.financial.mcp.postgres.entity.ToolRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolRegistryJpaRepository extends JpaRepository<ToolRegistryEntity, String> {
    ToolRegistryEntity findByToolId(String toolId);
}
