# MCP Spoke Server SDK - Architecture Deep Dive

## System Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────────┐
│                     External Clients                             │
│  (LLM Agents, API Gateways, Direct Callers)                     │
└────────────────┬──────────────────────────────────────────────┬─┘
                 │                                              │
         ┌───────▼────────┐                          ┌──────────▼──────┐
         │  JSON-RPC 2.0  │                          │   REST Adapter  │
         │  /mcp/rpc      │                          │   /api/**       │
         └───────┬────────┘                          └──────────┬──────┘
                 │                                              │
                 └──────────────────┬───────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  RestToJsonRpcConverter        │
                    │  (Protocol Adapter)            │
                    └───────────────┬────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  JsonRpcDispatcher             │
                    │  (Core Business Logic)         │
                    └───────────────┬────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ Validation       │      │ Authorization    │      │ Kill Switch      │
│ Pipeline        │      │ & Policy         │      │ Enforcement      │
└──────────────────┘      └──────────────────┘      └──────────────────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  JsonRpcHandler                │
                    │  (Tool Implementation)         │
                    └───────────────┬────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ Data Masking     │      │ Async Audit      │      │ Response         │
│ Service          │      │ Logging          │      │ Serialization    │
└──────────────────┘      └──────────────────┘      └──────────────────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  JsonRpcResponse               │
                    │  (Masked & Audited)            │
                    └────────────────────────────────┘
```

## Request Processing Pipeline

### Phase 1: Protocol Parsing

```java
// Input: Raw HTTP request
POST /mcp/rpc
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "api.ifrs17.loss_projection",
  "params": {...},
  "id": "uuid-v7",
  "meta": {...}
}

// Output: JsonRpcRequest object
JsonRpcRequest request = objectMapper.readValue(body, JsonRpcRequest.class);
```

**Validation:**
- Valid JSON syntax
- All required fields present
- Correct data types

### Phase 2: JSON-RPC Structure Validation

```java
request.validate();
// Checks:
// - jsonrpc == "2.0"
// - method is not null/blank
// - id is not null/blank (UUID v7 format)
// - meta is not null
```

**Enforcement:**
- Batch calls: REJECTED
- Notification calls (no id): REJECTED
- Invalid jsonrpc version: REJECTED

### Phase 3: Meta Field Validation

```java
meta.validate();
// Checks:
// - user_id: not null/blank
// - caller_id: not null/blank
// - trace_id: not null/blank (UUID v7)
// - dept: not null/blank
```

**Purpose:**
- Trace correlation across systems
- User identification for audit
- Department-level access control

### Phase 4: Global Kill Switch Check

```java
killSwitchService.validateGlobalNotDisabled();
// Query: Redis key "kill_switch:global"
// If disabled: throw McpException(TOOL_DISABLED)
```

**Effect:**
- Blocks ALL requests immediately
- Used for critical incidents
- Synchronized across all instances

### Phase 5: Tool Registry Lookup

```java
String toolId = extractToolId(request.getMethod()); // "ifrs17.loss_projection"
ToolRegistry tool = toolRegistryService.getToolRegistry(toolId);
// Lookup order:
// 1. Redis cache (key: "tools:ifrs17.loss_projection")
// 2. PostgreSQL fallback
// 3. Cache miss: throw TOOL_NOT_FOUND
```

**Cache Strategy:**
- TTL: 60 minutes
- Invalidation: Manual via `repository.save()`
- Fallback: PostgreSQL query

### Phase 6: Tool Kill Switch Check

```java
killSwitchService.validateToolNotDisabled(toolId);
// Query: Redis key "kill_switch:tool:ifrs17.loss_projection"
// If disabled: throw McpException(TOOL_DISABLED)
```

**Use Cases:**
- Security vulnerability in tool
- Maintenance window
- Data quality issues

### Phase 7: Authorization Validation

```java
policyService.validatePolicy(request.getMeta(), toolId);
// Lookup order:
// 1. Redis cache (key: "policy:user_id:tool_id")
// 2. PostgreSQL fallback
// 3. Cache miss: throw POLICY_DENIED
```

**Policy Check:**
- User must have `allowed=true` for tool
- Data level must match user's clearance
- RBAC + ABAC enforcement

### Phase 8: Input Schema Validation

```java
JsonNode schema = tool.getInputSchema();
schemaValidator.validate(request.getParams(), schema);
// Checks:
// - Required fields present
// - Field types match schema
// - No extra fields (optional)
```

**Schema Format:**
```json
{
  "type": "object",
  "required": ["portfolio_value", "loss_rate"],
  "properties": {
    "portfolio_value": {"type": "number"},
    "loss_rate": {"type": "number"}
  }
}
```

### Phase 9: Handler Execution

```java
Object result = handler.handle(request);
// Delegates to tool-specific implementation
// Example: Ifrs17LossProjectionHandler
```

**Error Handling:**
- Exceptions caught and mapped to error codes
- Latency tracked for audit
- Partial failures logged

### Phase 10: Data Masking

```java
DataMaskingPolicy maskingPolicy = policyService.getDataMaskingPolicy(
    request.getMeta().getUserId(),
    toolId
);
Object maskedResult = maskingService.maskData(result, maskingPolicy);
// Applies column-level masking:
// - HASH: SHA-256 hash
// - REDACT: ***REDACTED***
// - PARTIAL: Show 25%, mask rest
```

**Masking Rules:**
- Applied AFTER handler execution
- Role-based per user
- Transparent to handler

### Phase 11: Async Audit Logging

```java
auditService.logCall(
    request.getMeta(),
    toolId,
    request.getMethod(),
    request.getParams(),
    resultCode,
    latencyMs,
    errorMessage
);
// Async execution via @Async
// Sends to Elasticsearch
// Non-blocking
```

**Audit Log Fields:**
- trace_id: Correlation ID
- user_id: User identifier
- tool_id: Tool called
- params_hash: SHA-256 of params (not raw)
- result_code: SUCCESS or error code
- latency_ms: Request duration
- timestamp: Unix milliseconds
- error_message: If failed

### Phase 12: Response Serialization

```java
JsonRpcResponse response = JsonRpcResponse.success(request.getId(), maskedResult);
// Returns:
// {
//   "jsonrpc": "2.0",
//   "result": {...masked...},
//   "id": "uuid-v7"
// }
```

## Error Handling Strategy

### Exception Mapping

```
McpException
├── INVALID_PARAMS (400)
│   └── JSON parsing, schema validation, meta validation
├── POLICY_DENIED (403)
│   └── Authorization failed, user not allowed
├── DATA_NOT_FOUND (404)
│   └── Tool not found, resource not found
├── TOOL_DISABLED (503)
│   └── Kill switch active, tool disabled
├── MCP_TIMEOUT (504)
│   └── Request timeout
├── UNAUTHORIZED (401)
│   └── JWT validation failed
└── MCP_INTERNAL_ERROR (500)
    └── Unexpected server error
```

### Error Response Format

```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "POLICY_DENIED",
    "message": "User not authorized to access tool: ifrs17.loss_projection",
    "retryable": false
  },
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Error Handling in Dispatcher

```java
try {
    // Validation pipeline
    request.validate();
    killSwitchService.validateGlobalNotDisabled();
    // ... more validation ...
    
    // Execute
    Object result = handler.handle(request);
    
    // Mask & audit
    Object masked = maskingService.maskData(result, policy);
    auditService.logCall(..., "SUCCESS", ...);
    
    return JsonRpcResponse.success(request.getId(), masked);
    
} catch (McpException e) {
    auditService.logCall(..., e.getCode(), e.getMessage());
    return JsonRpcResponse.error(request.getId(), e.getCode(), e.getMessage(), e.isRetryable());
    
} catch (IllegalArgumentException e) {
    auditService.logCall(..., "INVALID_PARAMS", e.getMessage());
    return JsonRpcResponse.error(request.getId(), "INVALID_PARAMS", e.getMessage(), false);
    
} catch (Exception e) {
    log.error("Unexpected error", e);
    auditService.logCall(..., "MCP_INTERNAL_ERROR", e.getMessage());
    return JsonRpcResponse.error(request.getId(), "MCP_INTERNAL_ERROR", "Internal server error", false);
}
```

## Data Flow: REST Adapter

### REST Request Conversion

```
REST Request
├── Path: /api/ifrs17/loss-projection
├── Headers:
│   ├── Authorization: Bearer <JWT>
│   ├── X-Client-Id: agent-001
│   ├── X-Trace-Id: trace-uuid
│   └── X-Dept: RISK
└── Body: {"portfolio_value": 1000000, ...}
         │
         ▼
RestToJsonRpcConverter
├── Extract user_id from JWT
├── Extract caller_id from X-Client-Id
├── Extract trace_id from X-Trace-Id
├── Extract dept from X-Dept
├── Convert path to method: "api.ifrs17.loss_projection"
├── Generate UUID v7 for id
└── Build JsonRpcRequest
         │
         ▼
JsonRpcDispatcher (same as JSON-RPC path)
         │
         ▼
REST Response
└── Body: {"field1": "value1", ...} (result only, no wrapper)
```

### REST Error Response

```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "POLICY_DENIED",
    "message": "User not authorized",
    "retryable": false
  },
  "id": "..."
}
```

## Caching Strategy

### Tool Registry Cache

```
Key: tools:{tool_id}
TTL: 60 minutes
Invalidation: Manual via repository.save()
Fallback: PostgreSQL query

Example:
redis> GET tools:ifrs17.loss_projection
{
  "toolId": "ifrs17.loss_projection",
  "toolName": "IFRS17 Loss Projection",
  "version": "1.0.0",
  "status": "ACTIVE",
  "inputSchema": {...}
}
```

### Policy Cache

```
Key: policy:{user_id}:{tool_id}
TTL: 30 minutes
Invalidation: Manual via repository.savePolicy()
Fallback: PostgreSQL query

Example:
redis> GET policy:user@company.com:ifrs17.loss_projection
{
  "userId": "user@company.com",
  "toolId": "ifrs17.loss_projection",
  "allowed": true,
  "dataLevel": "CONFIDENTIAL"
}
```

### Masking Policy Cache

```
Key: masking:{user_id}:{tool_id}
TTL: 30 minutes
Invalidation: Manual via repository.saveMaskingPolicy()
Fallback: PostgreSQL query

Example:
redis> GET masking:user@company.com:ifrs17.loss_projection
{
  "userId": "user@company.com",
  "toolId": "ifrs17.loss_projection",
  "columnMasks": {
    "portfolio_value": "PARTIAL",
    "loss_rate": "HASH"
  },
  "dataLevel": "CONFIDENTIAL"
}
```

### Kill Switch Cache

```
Key: kill_switch:tool:{tool_id}
Key: kill_switch:global
TTL: None (persistent until cleared)
Invalidation: Manual via killSwitchService.enableTool()

Example:
redis> GET kill_switch:tool:ifrs17.loss_projection
{
  "targetId": "ifrs17.loss_projection",
  "disabled": true,
  "reason": "Security vulnerability detected",
  "disabledAt": 1702000000000
}
```

## Audit Logging Architecture

### Async Logging Flow

```
JsonRpcDispatcher
├── Record start time
├── Execute request
├── Record end time
├── Calculate latency
└── Call auditService.logCall() (async)
         │
         ▼
@Async auditService.logCall()
├── Hash params (SHA-256)
├── Build AuditLog object
└── Call repository.save() (async)
         │
         ▼
ElasticsearchAuditRepository
├── Serialize to JSON
└── Index to Elasticsearch
         │
         ▼
Elasticsearch Index: mcp-audit
└── Searchable audit trail
```

### Audit Log Indexing

```
Index: mcp-audit
Mapping:
{
  "properties": {
    "trace_id": {"type": "keyword"},
    "user_id": {"type": "keyword"},
    "tool_id": {"type": "keyword"},
    "result_code": {"type": "keyword"},
    "latency_ms": {"type": "long"},
    "timestamp": {"type": "date"},
    "dept": {"type": "keyword"}
  }
}
```

### Audit Queries

```
# All calls by user
GET mcp-audit/_search
{
  "query": {
    "term": {"user_id": "user@company.com"}
  }
}

# Failed calls
GET mcp-audit/_search
{
  "query": {
    "term": {"result_code": "POLICY_DENIED"}
  }
}

# Slow calls (>1000ms)
GET mcp-audit/_search
{
  "query": {
    "range": {"latency_ms": {"gte": 1000}}
  }
}
```

## Security Layers

### Layer 1: Transport Security

- HTTPS/TLS for all communication
- Certificate pinning (optional)
- Mutual TLS for service-to-service

### Layer 2: Authentication

- OAuth2 JWT validation
- JWKS endpoint verification
- Token expiration check
- Signature verification (RSA)

### Layer 3: Authorization

- RBAC: Role-based tool access
- ABAC: Attribute-based data levels
- Policy caching with fallback
- Deny-by-default

### Layer 4: Data Protection

- Column-level masking
- SHA-256 hashing
- Partial redaction
- Role-based visibility

### Layer 5: Audit & Compliance

- Server-side audit logging
- Params hashing (not raw values)
- Trace correlation
- Elasticsearch indexing

### Layer 6: Operational Safety

- Kill switch enforcement
- Tool-level disable
- Global disable
- Immediate effect

## Performance Characteristics

### Latency Breakdown (Typical)

```
JSON Parsing:           1-2ms
Validation:             2-5ms
Cache Lookup:           1-3ms
Authorization:          2-4ms
Schema Validation:      1-2ms
Handler Execution:      50-500ms (varies)
Data Masking:           1-3ms
Audit Logging:          <1ms (async)
Serialization:          1-2ms
─────────────────────────────
Total (p50):            60-520ms
Total (p99):            100-1000ms
```

### Throughput

- Single instance: 100-500 req/s (depends on handler)
- Horizontal scaling: Linear with instances
- Redis bottleneck: Unlikely (high throughput)
- PostgreSQL bottleneck: Possible at scale (use connection pooling)

### Resource Usage

- Memory: ~500MB base + handler-specific
- CPU: Low (mostly I/O bound)
- Network: Depends on payload size
- Disk: Elasticsearch storage for audit logs

## Deployment Topology

### Single Instance

```
┌─────────────────────────────────┐
│  MCP Spoke Server               │
│  ├── Spring Boot 3.x            │
│  ├── JSON-RPC Dispatcher        │
│  └── REST Adapter               │
└────────┬────────────────────────┘
         │
    ┌────┴────┬────────┬──────────┐
    │          │        │          │
    ▼          ▼        ▼          ▼
PostgreSQL  Redis  Elasticsearch  OAuth2
```

### High Availability

```
┌──────────────────────────────────────────────────┐
│  Load Balancer (Round Robin)                     │
└────────┬──────────────────────────────────────┬──┘
         │                                      │
    ┌────▼────────┐                    ┌───────▼────┐
    │ MCP Spoke 1  │                    │ MCP Spoke 2 │
    └────┬────────┘                    └───────┬────┘
         │                                      │
    ┌────┴──────────────────────────────────────┴──┐
    │                                              │
    ▼                                              ▼
PostgreSQL (Primary)                         Redis Cluster
    │                                              │
    ▼                                              ▼
PostgreSQL (Replica)                    Elasticsearch Cluster
```

## Testing Strategy

### Unit Tests

- Validation pipeline
- Error mapping
- Data masking logic
- Audit logging

### Integration Tests

- JSON-RPC dispatcher
- REST adapter
- Database repositories
- Cache behavior

### End-to-End Tests

- Full request flow
- Authorization enforcement
- Kill switch behavior
- Audit logging

### Load Tests

- Throughput: 1000 req/s
- Latency: p99 < 1000ms
- Cache hit rate: >90%
- Error rate: <0.1%

## Monitoring & Observability

### Metrics

- Request count (by tool, by user)
- Request latency (p50, p95, p99)
- Error rate (by error code)
- Cache hit rate
- Authorization denials
- Kill switch activations

### Logs

- Request/response (debug level)
- Errors (error level)
- Authorization decisions (info level)
- Kill switch events (warn level)

### Traces

- Distributed tracing via trace_id
- Correlation across services
- Latency breakdown
- Error propagation

### Alerts

- Error rate > 1%
- Latency p99 > 2000ms
- Kill switch active
- Cache miss rate > 10%
- Authorization denial spike
