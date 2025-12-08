package com.financial.mcp.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.registry.ToolRegistry;
import com.financial.mcp.core.registry.ToolRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisToolRegistryRepository implements ToolRegistryRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ToolRegistryRepository fallbackRepository;
    private static final String CACHE_KEY_PREFIX = "tools:";
    private static final long CACHE_TTL_MINUTES = 60;

    @Override
    public ToolRegistry findByToolId(String toolId) {
        String cacheKey = CACHE_KEY_PREFIX + toolId;
        
        // Try Redis first
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return objectMapper.convertValue(cached, ToolRegistry.class);
        }

        // Fallback to database
        ToolRegistry tool = fallbackRepository.findByToolId(toolId);
        if (tool != null) {
            redisTemplate.opsForValue().set(cacheKey, tool, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return tool;
    }

    @Override
    public void save(ToolRegistry tool) {
        fallbackRepository.save(tool);
        String cacheKey = CACHE_KEY_PREFIX + tool.getToolId();
        redisTemplate.opsForValue().set(cacheKey, tool, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void delete(String toolId) {
        fallbackRepository.delete(toolId);
        String cacheKey = CACHE_KEY_PREFIX + toolId;
        redisTemplate.delete(cacheKey);
    }
}
