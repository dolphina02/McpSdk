package com.financial.mcp.postgres.repository;

import com.financial.mcp.core.policy.DataMaskingPolicy;
import com.financial.mcp.core.policy.PolicyRepository;
import com.financial.mcp.core.policy.ToolPolicy;
import com.financial.mcp.postgres.entity.DataMaskingPolicyEntity;
import com.financial.mcp.postgres.entity.ToolPolicyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostgresPolicyRepository implements PolicyRepository {
    private final PolicyJpaRepository policyJpaRepository;
    private final MaskingPolicyJpaRepository maskingJpaRepository;

    @Override
    public ToolPolicy findPolicyByUserAndTool(String userId, String toolId) {
        ToolPolicyEntity entity = policyJpaRepository.findByUserIdAndToolId(userId, toolId);
        if (entity == null) {
            return null;
        }
        return mapToPolicy(entity);
    }

    @Override
    public ToolPolicy findPolicyByUserToolAndVersion(String userId, String toolId, String version) {
        ToolPolicyEntity entity = policyJpaRepository.findByUserIdAndToolIdAndVersion(userId, toolId, version);
        if (entity == null) {
            return null;
        }
        return mapToPolicy(entity);
    }

    @Override
    public DataMaskingPolicy findMaskingPolicy(String userId, String toolId) {
        DataMaskingPolicyEntity entity = maskingJpaRepository.findByUserIdAndToolId(userId, toolId);
        if (entity == null) {
            return null;
        }
        return mapToMaskingPolicy(entity);
    }

    @Override
    public DataMaskingPolicy findMaskingPolicyByVersion(String userId, String toolId, String version) {
        DataMaskingPolicyEntity entity = maskingJpaRepository.findByUserIdAndToolIdAndVersion(userId, toolId, version);
        if (entity == null) {
            return null;
        }
        return mapToMaskingPolicy(entity);
    }

    @Override
    public void savePolicy(ToolPolicy policy) {
        ToolPolicyEntity entity = ToolPolicyEntity.builder()
                .userId(policy.getUserId())
                .toolId(policy.getToolId())
                .version(policy.getVersion())
                .allowed(policy.isAllowed())
                .dataLevel(policy.getDataLevel())
                .createdAt(policy.getCreatedAt())
                .updatedAt(System.currentTimeMillis())
                .build();
        policyJpaRepository.save(entity);
    }

    @Override
    public void saveMaskingPolicy(DataMaskingPolicy policy) {
        DataMaskingPolicyEntity entity = DataMaskingPolicyEntity.builder()
                .userId(policy.getUserId())
                .toolId(policy.getToolId())
                .version(policy.getVersion())
                .columnMasks(policy.getColumnMasks())
                .dataLevel(policy.getDataLevel())
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
        maskingJpaRepository.save(entity);
    }

    private ToolPolicy mapToPolicy(ToolPolicyEntity entity) {
        return ToolPolicy.builder()
                .userId(entity.getUserId())
                .toolId(entity.getToolId())
                .version(entity.getVersion())
                .allowed(entity.isAllowed())
                .dataLevel(entity.getDataLevel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private DataMaskingPolicy mapToMaskingPolicy(DataMaskingPolicyEntity entity) {
        return DataMaskingPolicy.builder()
                .userId(entity.getUserId())
                .toolId(entity.getToolId())
                .version(entity.getVersion())
                .columnMasks(entity.getColumnMasks())
                .dataLevel(entity.getDataLevel())
                .build();
    }
}
