package com.financial.mcp.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.audit.AuditLog;
import com.financial.mcp.core.audit.AuditRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.StringReader;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ElasticsearchAuditRepository implements AuditRepository {
    private final ElasticsearchClient client;
    private final ObjectMapper objectMapper;
    private static final String INDEX_NAME = "mcp-audit";

    @Override
    public void save(AuditLog auditLog) {
        try {
            String logJson = objectMapper.writeValueAsString(auditLog);
            IndexRequest<Object> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(auditLog.getTraceId())
                    .withJson(new StringReader(logJson))
            );
            client.index(request);
        } catch (Exception e) {
            log.error("Failed to index audit log to Elasticsearch", e);
        }
    }
}
