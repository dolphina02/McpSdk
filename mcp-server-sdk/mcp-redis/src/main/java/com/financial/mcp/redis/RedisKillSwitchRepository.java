package com.financial.mcp.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.killswitch.KillSwitchRepository;
import com.financial.mcp.core.killswitch.KillSwitchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisKillSwitchRepository implements KillSwitchRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOOL_STATUS_PREFIX = "kill_switch:tool:";
    private static final String TOOL_VERSION_STATUS_PREFIX = "kill_switch:tool_version:";
    private static final String GLOBAL_STATUS_KEY = "kill_switch:global";

    @Override
    public KillSwitchStatus getToolStatus(String toolId) {
        String key = TOOL_STATUS_PREFIX + toolId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, KillSwitchStatus.class);
        }
        return null;
    }

    @Override
    public KillSwitchStatus getToolVersionStatus(String toolId, String version) {
        String key = TOOL_VERSION_STATUS_PREFIX + toolId + ":" + version;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return objectMapper.convertValue(cached, KillSwitchStatus.class);
        }
        return null;
    }

    @Override
    public KillSwitchStatus getGlobalStatus() {
        Object cached = redisTemplate.opsForValue().get(GLOBAL_STATUS_KEY);
        if (cached != null) {
            return objectMapper.convertValue(cached, KillSwitchStatus.class);
        }
        return null;
    }

    @Override
    public void setToolStatus(String toolId, boolean disabled, String reason) {
        String key = TOOL_STATUS_PREFIX + toolId;
        KillSwitchStatus status = KillSwitchStatus.builder()
                .targetId(toolId)
                .disabled(disabled)
                .reason(reason)
                .disabledAt(System.currentTimeMillis())
                .build();
        redisTemplate.opsForValue().set(key, status);
    }

    @Override
    public void setToolVersionStatus(String toolId, String version, boolean disabled, String reason) {
        String key = TOOL_VERSION_STATUS_PREFIX + toolId + ":" + version;
        KillSwitchStatus status = KillSwitchStatus.builder()
                .targetId(toolId + ":" + version)
                .disabled(disabled)
                .reason(reason)
                .disabledAt(System.currentTimeMillis())
                .build();
        redisTemplate.opsForValue().set(key, status);
    }

    @Override
    public void setGlobalStatus(boolean disabled, String reason) {
        KillSwitchStatus status = KillSwitchStatus.builder()
                .targetId("GLOBAL")
                .disabled(disabled)
                .reason(reason)
                .disabledAt(System.currentTimeMillis())
                .build();
        redisTemplate.opsForValue().set(GLOBAL_STATUS_KEY, status);
    }
}
