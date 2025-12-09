package com.financial.mcp.core.idempotency;

/**
 * Transaction state for idempotency tracking.
 */
public enum IdempotencyState {
    PROCESSING,
    COMPLETED,
    FAILED
}
