package com.financial.mcp.core.idempotency;

import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository repository;

    /**
     * Check if transaction is duplicate and mark as PROCESSING.
     * Uses Redis SETNX for atomic operation.
     * 
     * @param txId Transaction ID (UUID v7)
     * @throws McpException if duplicate transaction detected
     */
    public void checkAndMarkProcessing(String txId) {
        boolean isNew = repository.setProcessing(txId);
        if (!isNew) {
            log.warn("Duplicate transaction detected: {}", txId);
            throw new McpException(
                    McpErrorCode.DUPLICATE_TX,
                    "Duplicate transaction detected",
                    false
            );
        }
    }

    /**
     * Mark transaction as completed after successful execution.
     * Extends TTL to 30 minutes for result caching.
     * 
     * @param txId Transaction ID
     */
    public void markCompleted(String txId) {
        repository.setCompleted(txId);
        log.debug("Transaction marked as completed: {}", txId);
    }

    /**
     * Mark transaction as failed.
     * Keeps TTL at 10 minutes for retry window.
     * 
     * @param txId Transaction ID
     */
    public void markFailed(String txId) {
        repository.setFailed(txId);
        log.debug("Transaction marked as failed: {}", txId);
    }
}
