package com.financial.mcp.postgres.repository;

import com.financial.mcp.postgres.entity.DataMaskingPolicyEntity;
import com.financial.mcp.postgres.entity.ToolPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyJpaRepository extends JpaRepository<ToolPolicyEntity, String> {
    ToolPolicyEntity findByUserIdAndToolId(String userId, String toolId);
}

@Repository
interface MaskingPolicyJpaRepository extends JpaRepository<DataMaskingPolicyEntity, String> {
    DataMaskingPolicyEntity findByUserIdAndToolId(String userId, String toolId);
}
