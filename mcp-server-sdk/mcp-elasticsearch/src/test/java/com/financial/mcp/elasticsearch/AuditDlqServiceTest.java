package com.financial.mcp.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.audit.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditDlqServiceTest {
    private AuditDlqService service;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AuditDlqService(objectMapper);
        // Override DLQ path to temp directory
        service.dlqPath = tempDir.toString();
    }

    @Test
    void testWriteToDlq_CreatesFileAndWritesLog() throws Exception {
        AuditLog auditLog = AuditLog.builder()
                .traceId("trace-123")
                .userId("user-456")
                .callerId("caller-789")
                .toolId("test.tool")
                .method("test.tool.execute")
                .paramsHash("hash123")
                .resultCode("SUCCESS")
                .latencyMs(100)
                .timestamp(System.currentTimeMillis())
                .dept("FINANCE")
                .build();

        service.writeToDlq(auditLog);

        // Verify file was created
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Path filePath = tempDir.resolve("audit-" + dateStr + ".log");
        assertTrue(Files.exists(filePath));

        // Verify content
        List<String> lines = Files.readAllLines(filePath);
        assertEquals(1, lines.size());
        
        AuditLog readLog = objectMapper.readValue(lines.get(0), AuditLog.class);
        assertEquals("trace-123", readLog.getTraceId());
        assertEquals("user-456", readLog.getUserId());
    }

    @Test
    void testWriteToDlq_AppendToExistingFile() throws Exception {
        AuditLog log1 = AuditLog.builder()
                .traceId("trace-1")
                .userId("user-1")
                .callerId("caller-1")
                .toolId("test.tool")
                .method("test.tool.execute")
                .paramsHash("hash1")
                .resultCode("SUCCESS")
                .latencyMs(100)
                .timestamp(System.currentTimeMillis())
                .dept("FINANCE")
                .build();

        AuditLog log2 = AuditLog.builder()
                .traceId("trace-2")
                .userId("user-2")
                .callerId("caller-2")
                .toolId("test.tool")
                .method("test.tool.execute")
                .paramsHash("hash2")
                .resultCode("SUCCESS")
                .latencyMs(150)
                .timestamp(System.currentTimeMillis())
                .dept("FINANCE")
                .build();

        service.writeToDlq(log1);
        service.writeToDlq(log2);

        // Verify both logs are in file
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Path filePath = tempDir.resolve("audit-" + dateStr + ".log");
        List<String> lines = Files.readAllLines(filePath);
        assertEquals(2, lines.size());
    }

    @Test
    void testWriteToDlq_CreatesDirectoryIfNotExists() throws Exception {
        Path customDlqPath = tempDir.resolve("custom/dlq/path");
        service.dlqPath = customDlqPath.toString();

        AuditLog auditLog = AuditLog.builder()
                .traceId("trace-123")
                .userId("user-456")
                .callerId("caller-789")
                .toolId("test.tool")
                .method("test.tool.execute")
                .paramsHash("hash123")
                .resultCode("SUCCESS")
                .latencyMs(100)
                .timestamp(System.currentTimeMillis())
                .dept("FINANCE")
                .build();

        service.writeToDlq(auditLog);

        assertTrue(Files.exists(customDlqPath));
        
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Path filePath = customDlqPath.resolve("audit-" + dateStr + ".log");
        assertTrue(Files.exists(filePath));
    }
}
