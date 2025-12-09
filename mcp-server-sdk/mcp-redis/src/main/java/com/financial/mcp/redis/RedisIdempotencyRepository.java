package com.financial.mcp.redis;

import com.financial.mcp.core.idempotency.IdempotencyRepository;
import com.financial.mcp.core.idempotency.IdempotencyState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisIdempotencyRepository implements IdempotencyRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "tx:";
    private static final long PROCESSING_TTL_MINUTES = 10;
    private static final long COMPLETED_TTL_MINUTES = 30;

    @Override
    public boolean setProcessing(String txId) {
        String key = KEY_PREFIX + txId;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(
                key,
                IdempotencyState.PROCESSING.name(),
                PROCESSING_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        return result != null && result;
    }

    @Override
    public void setCompleted(String txId) {
        String key = KEY_PREFIX + txId;
        redisTemplate.opsForValue().set(
                key,
                IdempotencyState.COMPLETED.name(),
                COMPLETED_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    @Override
    public void setFailed(String txId) {
        String key = KEY_PREFIX + txId;
        redisTemplate.opsForValue().set(
                key,
                IdempotencyState.FAILED.name(),
                PROCESSING_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    @Override
    public IdempotencyState getState(String txId) {
        String key = KEY_PREFIX + txId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        try {
            return IdempotencyState.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid idempotency state in Redis: {}", value);
            return null;
        }
    }
}
