package com.financial.mcp.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.policy.DataMaskingPolicy;
import com.financial.mcp.core.policy.PolicyRepository;
import com.financial.mcp.core.policy.ToolPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisPolicyRepository implements PolicyRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PolicyRepository fallbackRepository;
    private static final String POLICY_PREFIX = "policy:";
    private static final String MASKING_PREFIX = "masking:";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public ToolPolicy findPolicyByUserAndTool(String userId, String toolId) {
        String key = POLICY_PREFIX + userId + ":" + toolId;
        
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, ToolPolicy.class);
        }

        ToolPolicy policy = fallbackRepository.findPolicyByUserAndTool(userId, toolId);
        if (policy != null) {
            redisTemplate.opsForValue().set(key, policy, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return policy;
    }

    @Override
    public ToolPolicy findPolicyByUserToolAndVersion(String userId, String toolId, String version) {
        String key = POLICY_PREFIX + userId + ":" + toolId + ":" + version;
        
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, ToolPolicy.class);
        }

        ToolPolicy policy = fallbackRepository.findPolicyByUserToolAndVersion(userId, toolId, version);
        if (policy != null) {
            redisTemplate.opsForValue().set(key, policy, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return policy;
    }

    @Override
    public DataMaskingPolicy findMaskingPolicy(String userId, String toolId) {
        String key = MASKING_PREFIX + userId + ":" + toolId;
        
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, DataMaskingPolicy.class);
        }

        DataMaskingPolicy policy = fallbackRepository.findMaskingPolicy(userId, toolId);
        if (policy != null) {
            redisTemplate.opsForValue().set(key, policy, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return policy;
    }

    @Override
    public DataMaskingPolicy findMaskingPolicyByVersion(String userId, String toolId, String version) {
        String key = MASKING_PREFIX + userId + ":" + toolId + ":" + version;
        
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, DataMaskingPolicy.class);
        }

        DataMaskingPolicy policy = fallbackRepository.findMaskingPolicyByVersion(userId, toolId, version);
        if (policy != null) {
            redisTemplate.opsForValue().set(key, policy, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return policy;
    }

    @Override
    public void savePolicy(ToolPolicy policy) {
        fallbackRepository.savePolicy(policy);
        String key = POLICY_PREFIX + policy.getUserId() + ":" + policy.getToolId();
        redisTemplate.opsForValue().set(key, policy, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void saveMaskingPolicy(DataMaskingPolicy policy) {
        fallbackRepository.saveMaskingPolicy(policy);
        String key = MASKING_PREFIX + policy.getUserId() + ":" + policy.getToolId();
        redisTemplate.opsForValue().set(key, policy, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }
}
