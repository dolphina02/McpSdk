package com.financial.mcp.core.audit;

public interface AuditRepository {
    void save(AuditLog log);
}
