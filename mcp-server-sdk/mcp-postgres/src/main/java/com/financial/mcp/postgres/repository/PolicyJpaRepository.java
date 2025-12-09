package com.financial.mcp.postgres.repository;

import com.financial.mcp.postgres.entity.DataMaskingPolicyEntity;
import com.financial.mcp.postgres.entity.ToolPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyJpaRepository extends JpaRepository<ToolPolicyEntity, String> {
    ToolPolicyEntity findByUserIdAndToolId(String userId, String toolId);
    
    /**
     * Find policy by user, tool, and version (composite key).
     */
    ToolPolicyEntity findByUserIdAndToolIdAndVersion(String userId, String toolId, String version);
}

@Repository
interface MaskingPolicyJpaRepository extends JpaRepository<DataMaskingPolicyEntity, String> {
    DataMaskingPolicyEntity findByUserIdAndToolId(String userId, String toolId);
    
    /**
     * Find masking policy by user, tool, and version (composite key).
     */
    DataMaskingPolicyEntity findByUserIdAndToolIdAndVersion(String userId, String toolId, String version);
}
