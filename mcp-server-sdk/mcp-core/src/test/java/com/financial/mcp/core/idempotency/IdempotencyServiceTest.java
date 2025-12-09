package com.financial.mcp.core.idempotency;

import com.financial.mcp.core.error.McpErrorCode;
import com.financial.mcp.core.error.McpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {
    @Mock
    private IdempotencyRepository repository;

    private IdempotencyService service;

    @BeforeEach
    void setUp() {
        service = new IdempotencyService(repository);
    }

    @Test
    void testCheckAndMarkProcessing_NewTransaction() {
        String txId = "550e8400-e29b-41d4-a716-446655440000";
        when(repository.setProcessing(txId)).thenReturn(true);

        // Should not throw
        service.checkAndMarkProcessing(txId);

        verify(repository).setProcessing(txId);
    }

    @Test
    void testCheckAndMarkProcessing_DuplicateTransaction() {
        String txId = "550e8400-e29b-41d4-a716-446655440000";
        when(repository.setProcessing(txId)).thenReturn(false);

        McpException exception = assertThrows(McpException.class, () -> {
            service.checkAndMarkProcessing(txId);
        });

        assertEquals(McpErrorCode.DUPLICATE_TX, exception.getCode());
        assertEquals("Duplicate transaction detected", exception.getMessage());
        assertFalse(exception.isRetryable());
    }

    @Test
    void testMarkCompleted() {
        String txId = "550e8400-e29b-41d4-a716-446655440000";

        service.markCompleted(txId);

        verify(repository).setCompleted(txId);
    }

    @Test
    void testMarkFailed() {
        String txId = "550e8400-e29b-41d4-a716-446655440000";

        service.markFailed(txId);

        verify(repository).setFailed(txId);
    }
}
