package com.financial.mcp.core.idempotency;

/**
 * Repository for idempotency state management.
 * Implementations must use atomic operations (e.g., Redis SETNX).
 */
public interface IdempotencyRepository {
    /**
     * Atomically set transaction to PROCESSING state.
     * Uses SETNX with 10-minute TTL.
     * 
     * @param txId Transaction ID
     * @return true if new key was set, false if already exists
     */
    boolean setProcessing(String txId);

    /**
     * Set transaction to COMPLETED state.
     * Extends TTL to 30 minutes for result caching.
     * 
     * @param txId Transaction ID
     */
    void setCompleted(String txId);

    /**
     * Set transaction to FAILED state.
     * Keeps TTL at 10 minutes for retry window.
     * 
     * @param txId Transaction ID
     */
    void setFailed(String txId);

    /**
     * Get current state of transaction.
     * 
     * @param txId Transaction ID
     * @return IdempotencyState or null if not found
     */
    IdempotencyState getState(String txId);
}
