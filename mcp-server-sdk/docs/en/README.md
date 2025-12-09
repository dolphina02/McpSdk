# MCP Spoke Server SDK (Spring Boot Starter)

A production-grade, financial-grade Model Context Protocol (MCP) Spoke Server SDK built with Spring Boot 3.x. This SDK provides a complete framework for building secure, auditable, and scalable MCP servers with dual JSON-RPC 2.0 and REST interfaces.

## What is This SDK?

### Purpose
This is a **Spring Boot Starter library** for building **MCP Spoke Servers** - the server-side component that exposes tools/capabilities to LLM agents and AI systems via the Model Context Protocol.

### MCP Layer
This SDK implements the **MCP Server (Spoke) Layer**:

```
┌─────────────────────────────────────────────────────────┐
│  LLM / AI Agent / Client Application                    │
└────────────────────┬────────────────────────────────────┘
                     │ (JSON-RPC 2.0 or REST)
                     │
        ┌────────────▼────────────┐
        │  MCP Spoke Server       │  ◄── THIS SDK
        │  (Your Implementation)  │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │  Your Business Logic    │
        │  (Tools/Capabilities)   │
        └─────────────────────────┘
```

### What You Build With This SDK
- **MCP Spoke Servers**: Standalone servers that expose tools to LLM agents
- **Tool Implementations**: Custom business logic wrapped as MCP tools
- **Secure APIs**: With OAuth2, RBAC, ABAC, and audit logging
- **Production-Ready**: With caching, kill switches, and monitoring

### Key Characteristics
- **Spoke-Only**: No hub component (hub is managed separately)
- **Dual Interface**: JSON-RPC 2.0 (native MCP) + REST (for API gateways)
- **Single Core**: All business logic in JSON-RPC dispatcher (no duplication)
- **Financial-Grade**: Security, audit, compliance built-in

## Architecture Overview

### Core Design Principles

- **Spoke-Only MCP**: No hub dependency. Single business logic core with dual protocol interfaces.
- **Protocol Separation**: JSON-RPC 2.0 is the canonical protocol. REST acts as a protocol adapter only.
- **Single Source of Truth**: All business logic lives in the JSON-RPC dispatcher. REST endpoints convert to JSON-RPC internally.
- **Financial-Grade Security**: OAuth2 JWT validation, RBAC+ABAC authorization, column-level data masking.
- **Audit Compliance**: Server-side audit logging with SHA-256 hashing, asynchronous Elasticsearch indexing.
- **Kill Switch Enforcement**: Tool-level and global disable with Redis + PostgreSQL synchronization.

### Module Structure

```
mcp-server-sdk/
├── mcp-core/                    # Core JSON-RPC engine & enforcement layers
│   ├── rpc/                     # JSON-RPC 2.0 dispatcher & request/response models
│   ├── validation/              # JSON Schema validator
│   ├── registry/                # Tool registry service (PostgreSQL + Redis cache)
│   ├── policy/                  # RBAC/ABAC policy service
│   ├── killswitch/              # Kill switch enforcement
│   ├── masking/                 # Column-level data masking
│   ├── audit/                   # Audit logging service
│   ├── error/                   # Standard error codes
│   └── meta/                    # MCP metadata model
├── mcp-rest-adapter/            # REST → JSON-RPC protocol adapter
├── mcp-security/                # OAuth2 JWT validation
├── mcp-redis/                   # Redis cache implementations
├── mcp-postgres/                # PostgreSQL repository implementations
├── mcp-elasticsearch/           # Elasticsearch audit logging
├── mcp-autoconfigure/           # Spring Boot auto-configuration
└── sample-spoke-app/            # Example implementation
```

## Execution Flow

### Request Validation Pipeline (STRICT ORDER)

```
1. JSON Parsing
   ↓
2. JSON-RPC Structure Validation (jsonrpc=2.0, method, params, id, meta)
   ↓
3. Meta Required-Field Validation (user_id, caller_id, trace_id, dept)
   ↓
4. Global Kill Switch Check
   ↓
5. Tool Registry Lookup (PostgreSQL → Redis cache)
   ↓
6. Tool Kill Switch Check
   ↓
7. Authorization Validation (Redis first → RDB fallback)
   ↓
8. Input JSON Schema Validation
   ↓
9. Execute Handler
   ↓
10. Apply Data Masking (role-based column masking)
    ↓
11. Async Audit Logging to Elasticsearch
    ↓
12. Return Masked Response
```

### JSON-RPC 2.0 Request Format

```json
{
  "jsonrpc": "2.0",
  "method": "namespace.tool_id",
  "params": {
    "field1": "value1",
    "field2": 123
  },
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "meta": {
    "user_id": "user@company.com",
    "caller_id": "agent-001",
    "trace_id": "trace-uuid-v7",
    "dept": "RISK"
  }
}
```

### JSON-RPC 2.0 Response Format

**Success:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "field1": "value1",
    "field2": 123
  },
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error:**
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

## Security Model

### Authentication

- **OAuth2 Resource Server**: Spring Security OAuth2 integration
- **JWT Validation**: Nimbus JOSE library with JWKS endpoint support
- **Token Introspection**: Automatic expiration and signature verification

### Authorization

- **RBAC**: Role-based access control via tool policies
- **ABAC**: Attribute-based access control via data levels (PUBLIC, INTERNAL, CONFIDENTIAL)
- **Policy Caching**: Redis cache with 30-minute TTL, PostgreSQL fallback

### Data Protection

- **Column-Level Masking**: Role-based dynamic masking (HASH, REDACT, PARTIAL)
- **SHA-256 Hashing**: Sensitive fields hashed before storage
- **Audit Trail**: All operations logged with params hash (not raw params)

## Kill Switch Behavior

### Tool-Level Kill Switch

```java
killSwitchService.disableTool("ifrs17.loss_projection", "Security vulnerability detected");
```

**Effect:**
- Blocks ALL calls to the tool (JSON-RPC and REST)
- Returns `TOOL_DISABLED` error code
- Synchronized across Redis + PostgreSQL
- Immediate effect (no cache delay)

### Global Kill Switch

```java
killSwitchService.disableGlobal("Critical infrastructure issue");
```

**Effect:**
- Blocks ALL MCP calls (all tools)
- Returns `TOOL_DISABLED` error code
- Immediate effect across all instances

### Status Queries

```java
KillSwitchStatus status = killSwitchService.getToolStatus("ifrs17.loss_projection");
if (status != null && status.isDisabled()) {
    // Tool is disabled
}
```

## Standard Error Codes

| Code | Meaning | Retryable | HTTP |
|------|---------|-----------|------|
| `INVALID_PARAMS` | Request validation failed | No | 400 |
| `POLICY_DENIED` | Authorization failed | No | 403 |
| `DATA_NOT_FOUND` | Resource not found | No | 404 |
| `TOOL_DISABLED` | Tool disabled by kill switch | No | 503 |
| `MCP_TIMEOUT` | Request timeout | Yes | 504 |
| `MCP_INTERNAL_ERROR` | Server error | No | 500 |
| `UNAUTHORIZED` | Authentication failed | No | 401 |
| `TOOL_NOT_FOUND` | Tool not registered | No | 404 |

## REST API Adapter

The REST adapter converts HTTP requests to JSON-RPC internally. All business logic remains in the JSON-RPC core.

### REST Endpoint Pattern

```
POST /api/{namespace}/{tool_id}
```

### REST Request Headers

```
Authorization: Bearer <JWT_TOKEN>
X-Client-Id: agent-001
X-Trace-Id: trace-uuid-v7
X-Dept: RISK
X-User-Id: user@company.com (optional, extracted from JWT if not provided)
```

### REST Request Body

```json
{
  "field1": "value1",
  "field2": 123
}
```

### REST Response

```json
{
  "field1": "value1",
  "field2": 123
}
```

### Error Response

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

## Audit Logging

### Audit Log Structure

```json
{
  "trace_id": "trace-uuid-v7",
  "user_id": "user@company.com",
  "caller_id": "agent-001",
  "tool_id": "ifrs17.loss_projection",
  "method": "api.ifrs17.loss_projection",
  "params_hash": "sha256_hash_of_params",
  "result_code": "SUCCESS",
  "latency_ms": 145,
  "timestamp": 1702000000000,
  "error_message": null,
  "dept": "RISK"
}
```

### Audit Features

- **Asynchronous**: Non-blocking async logging via Spring `@Async`
- **Elasticsearch**: Indexed for compliance and forensics
- **Params Hashing**: SHA-256 hash of params (not raw values)
- **Latency Tracking**: Request duration in milliseconds
- **Error Capture**: Error messages logged for failed calls
- **Trace Correlation**: All logs linked via trace_id

## Tool Registry

### Tool Registration

```java
ToolRegistry tool = ToolRegistry.builder()
    .toolId("ifrs17.loss_projection")
    .toolName("IFRS17 Loss Projection")
    .version("1.0.0")
    .status("ACTIVE")
    .inputSchema(jsonSchema)
    .description("Calculate IFRS17 loss projections")
    .createdAt(System.currentTimeMillis())
    .updatedAt(System.currentTimeMillis())
    .build();

repository.save(tool);
```

### Tool Status Values

- `ACTIVE`: Tool is available
- `DISABLED`: Tool is disabled (use kill switch instead)

### Input Schema

JSON Schema format for request validation:

```json
{
  "type": "object",
  "required": ["portfolio_value", "loss_rate", "projection_years"],
  "properties": {
    "portfolio_value": {"type": "number"},
    "loss_rate": {"type": "number"},
    "projection_years": {"type": "integer"}
  }
}
```

## Policy Management

### Tool Policy

```java
ToolPolicy policy = ToolPolicy.builder()
    .userId("user@company.com")
    .toolId("ifrs17.loss_projection")
    .allowed(true)
    .dataLevel("CONFIDENTIAL")
    .createdAt(System.currentTimeMillis())
    .updatedAt(System.currentTimeMillis())
    .build();

policyRepository.savePolicy(policy);
```

### Data Masking Policy

```java
DataMaskingPolicy maskingPolicy = DataMaskingPolicy.builder()
    .userId("user@company.com")
    .toolId("ifrs17.loss_projection")
    .columnMasks(Map.of(
        "portfolio_value", "PARTIAL",
        "loss_rate", "HASH"
    ))
    .dataLevel("CONFIDENTIAL")
    .build();

policyRepository.saveMaskingPolicy(maskingPolicy);
```

### Masking Types

- `HASH`: SHA-256 hash (first 16 chars)
- `REDACT`: Replace with `***REDACTED***`
- `PARTIAL`: Show first 25%, mask rest

## Database Schema

### tool_registry

```sql
CREATE TABLE tool_registry (
    id UUID PRIMARY KEY,
    tool_id VARCHAR(255) UNIQUE NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    input_schema JSONB,
    description TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);
```

### tool_policy

```sql
CREATE TABLE tool_policy (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    allowed BOOLEAN NOT NULL,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);
```

### data_masking_policy

```sql
CREATE TABLE data_masking_policy (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    column_masks JSONB,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);
```

## Configuration

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mcp_db
    username: postgres
    password: postgres
  
  redis:
    host: localhost
    port: 6379
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-provider.com
          jwk-set-uri: https://your-auth-provider.com/.well-known/jwks.json

server:
  port: 8080
```

## Usage Example

### 1. Implement JsonRpcHandler

```java
@Component
public class MyToolHandler implements JsonRpcHandler {
    @Override
    public Object handle(JsonRpcRequest request) {
        // Your business logic
        return result;
    }
}
```

### 2. Register Tool

```java
@Component
public class ToolInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        ToolRegistry tool = ToolRegistry.builder()
            .toolId("my.tool")
            .toolName("My Tool")
            .version("1.0.0")
            .status("ACTIVE")
            .inputSchema(schema)
            .build();
        
        repository.save(tool);
    }
}
```

### 3. Call via JSON-RPC

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.my.tool",
    "params": {"field": "value"},
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

### 4. Call via REST

```bash
curl -X POST http://localhost:8080/api/my/tool \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid" \
  -H "X-Dept: RISK" \
  -d '{"field": "value"}'
```

## Performance Considerations

- **Redis Caching**: Tool registry and policies cached for 30-60 minutes
- **Async Audit Logging**: Non-blocking Elasticsearch indexing
- **Connection Pooling**: Lettuce for Redis, HikariCP for PostgreSQL
- **Batch Calls**: Not supported (per MCP spec)
- **Notifications**: Not supported (per MCP spec)

## Production Deployment

### Prerequisites

- Java 17+
- PostgreSQL 12+
- Redis 6+
- Elasticsearch 8+
- OAuth2 provider (Auth0, Okta, etc.)

### Environment Variables

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/mcp
SPRING_DATASOURCE_USERNAME=mcp_user
SPRING_DATASOURCE_PASSWORD=<secure_password>
SPRING_REDIS_HOST=prod-redis
SPRING_REDIS_PORT=6379
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://auth.company.com
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://auth.company.com/.well-known/jwks.json
```

### Docker Deployment

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY sample-spoke-app/build/libs/sample-spoke-app-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Troubleshooting

### Tool Not Found

- Verify tool is registered in `tool_registry` table
- Check Redis cache: `redis-cli get tools:tool_id`
- Verify method format: `namespace.tool_id`

### Authorization Denied

- Check `tool_policy` table for user + tool combination
- Verify user has `allowed=true`
- Check Redis cache: `redis-cli get policy:user_id:tool_id`

### Kill Switch Active

- Query: `SELECT * FROM kill_switch WHERE target_id = 'tool_id'`
- Or: `redis-cli get kill_switch:tool:tool_id`
- Re-enable: `killSwitchService.enableTool(toolId)`

### Audit Logs Missing

- Verify Elasticsearch is running
- Check index: `curl http://localhost:9200/mcp-audit/_search`
- Verify async executor: `@EnableAsync` on main class

## License

Proprietary - Financial Grade MCP SDK
