# MCP Spoke Server SDK - Quick Start Guide

## Prerequisites

- Java 17+
- Gradle 8+
- Docker & Docker Compose (for local services)
- Git

## Local Development Setup

### 1. Start Infrastructure Services

```bash
# Create docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mcp_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

volumes:
  postgres_data:
  elasticsearch_data:
EOF

# Start services
docker-compose up -d
```

### 2. Initialize Database

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d mcp_db

# Create tables
CREATE TABLE tool_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tool_id VARCHAR(255) UNIQUE NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    input_schema JSONB,
    description TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE TABLE tool_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    allowed BOOLEAN NOT NULL,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);

CREATE TABLE data_masking_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    column_masks JSONB,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);

\q
```

### 3. Build the Project

```bash
cd mcp-server-sdk

# Build all modules
./gradlew clean build

# Build only sample app
./gradlew :sample-spoke-app:bootJar
```

### 4. Run Sample Application

```bash
# Run with development profile (JWT validation disabled, all requests allowed)
./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'
```

Application starts at `http://localhost:8080`

## Testing the API

### 1. Generate Test JWT Token (Development Mode)

The development mode provides automatic JWT token generation endpoints:

```bash
# Get default test token
curl http://localhost:8080/dev/token

# Response:
# {
#   "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "user_id": "user@company.com",
#   "dept": "RISK",
#   "roles": ["ADMIN", "USER"],
#   "expires_in": 86400,
#   "message": "Development token - Valid for 24 hours"
# }
```

### 1a. Generate Custom Token

```bash
# Generate token for specific user
curl -X POST 'http://localhost:8080/dev/token?user_id=alice@company.com&dept=COMPLIANCE&roles=USER'

# Response:
# {
#   "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "user_id": "alice@company.com",
#   "dept": "COMPLIANCE",
#   "roles": ["USER"],
#   "expires_in": 86400
# }
```

### 1b. View Token Generation Help

```bash
curl http://localhost:8080/dev/token/help
```

### 1c. Store Token in Variable

```bash
# Get token and store in variable
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')
echo "Token: $TOKEN"
```

### 2. Test JSON-RPC Endpoint

```bash
# Get token first
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')

# Call JSON-RPC endpoint
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {
      "portfolio_value": 1000000,
      "loss_rate": 0.05,
      "projection_years": 3
    },
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid-v7",
      "dept": "RISK"
    }
  }'
```

Or without storing token:

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(curl -s http://localhost:8080/dev/token | jq -r '.token')" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {
      "portfolio_value": 1000000,
      "loss_rate": 0.05,
      "projection_years": 3
    },
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid-v7",
      "dept": "RISK"
    }
  }'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "portfolio_value": 1000000,
    "loss_rate": 0.05,
    "projection_years": 3,
    "projected_loss": 150000,
    "remaining_value": 850000,
    "confidence_level": 0.95
  },
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. Test REST Endpoint

```bash
# Get token first
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')

# Call REST endpoint
curl -X POST http://localhost:8080/api/ifrs17/loss-projection \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid-v7" \
  -H "X-Dept: RISK" \
  -d '{
    "portfolio_value": 1000000,
    "loss_rate": 0.05,
    "projection_years": 3
  }'
```

Or in one line:

```bash
curl -X POST http://localhost:8080/api/ifrs17/loss-projection \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(curl -s http://localhost:8080/dev/token | jq -r '.token')" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid-v7" \
  -H "X-Dept: RISK" \
  -d '{
    "portfolio_value": 1000000,
    "loss_rate": 0.05,
    "projection_years": 3
  }'
```

**Expected Response:**
```json
{
  "portfolio_value": 1000000,
  "loss_rate": 0.05,
  "projection_years": 3,
  "projected_loss": 150000,
  "remaining_value": 850000,
  "confidence_level": 0.95
}
```

### 4. Test Authorization Denial

```bash
# First, create a policy that denies access
psql -h localhost -U postgres -d mcp_db << 'EOF'
INSERT INTO tool_policy (user_id, tool_id, allowed, data_level, created_at, updated_at)
VALUES ('denied_user@company.com', 'ifrs17.loss_projection', false, 'PUBLIC', 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000, 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);
EOF

# Call with denied user
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3},
    "id": "test-id",
    "meta": {
      "user_id": "denied_user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "POLICY_DENIED",
    "message": "User not authorized to access tool: ifrs17.loss_projection",
    "retryable": false
  },
  "id": "test-id"
}
```

### 5. Test Kill Switch

```bash
# Disable tool
curl -X POST http://localhost:8080/admin/killswitch/disable \
  -H "Content-Type: application/json" \
  -d '{
    "tool_id": "ifrs17.loss_projection",
    "reason": "Security vulnerability"
  }'

# Try to call disabled tool
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3},
    "id": "test-id",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "TOOL_DISABLED",
    "message": "Tool is disabled by kill switch: ifrs17.loss_projection",
    "retryable": false
  },
  "id": "test-id"
}
```

## Implementing Your Own Tool

### Step 1: Create Handler

```java
// src/main/java/com/financial/mcp/sample/tool/MyToolHandler.java
package com.financial.mcp.sample.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.rpc.JsonRpcHandler;
import com.financial.mcp.core.rpc.JsonRpcRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MyToolHandler implements JsonRpcHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Object handle(JsonRpcRequest request) {
        if ("api.my.namespace.my_tool".equals(request.getMethod())) {
            return handleMyTool(request.getParams());
        }
        throw new IllegalArgumentException("Unknown method: " + request.getMethod());
    }

    private Object handleMyTool(Object params) {
        Map<String, Object> paramsMap = objectMapper.convertValue(params, Map.class);
        
        // Your business logic here
        String input = (String) paramsMap.get("input");
        
        Map<String, Object> result = new HashMap<>();
        result.put("input", input);
        result.put("output", "Processed: " + input);
        
        return result;
    }
}
```

### Step 2: Register Tool

```java
// src/main/java/com/financial/mcp/sample/config/MyToolRegistry.java
package com.financial.mcp.sample.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.registry.ToolRegistry;
import com.financial.mcp.core.registry.ToolRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyToolRegistry implements CommandLineRunner {
    private final ToolRegistryRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        String schemaJson = """
                {
                  "type": "object",
                  "required": ["input"],
                  "properties": {
                    "input": {"type": "string"}
                  }
                }
                """;

        JsonNode schema = objectMapper.readTree(schemaJson);

        ToolRegistry tool = ToolRegistry.builder()
                .toolId("my.namespace.my_tool")
                .toolName("My Tool")
                .version("1.0.0")
                .status("ACTIVE")
                .inputSchema(schema)
                .description("My custom tool")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        repository.save(tool);
    }
}
```

### Step 3: Grant Access

```bash
psql -h localhost -U postgres -d mcp_db << 'EOF'
INSERT INTO tool_policy (user_id, tool_id, allowed, data_level, created_at, updated_at)
VALUES ('user@company.com', 'my.namespace.my_tool', true, 'PUBLIC', 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000, 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);
EOF
```

### Step 4: Test

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.my.namespace.my_tool",
    "params": {"input": "hello"},
    "id": "test-id",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

## Viewing Audit Logs

### Query Elasticsearch

```bash
# All logs
curl http://localhost:9200/mcp-audit/_search

# Logs for specific user
curl http://localhost:9200/mcp-audit/_search -d '{
  "query": {
    "term": {"user_id": "user@company.com"}
  }
}'

# Failed calls
curl http://localhost:9200/mcp-audit/_search -d '{
  "query": {
    "term": {"result_code": "POLICY_DENIED"}
  }
}'

# Slow calls (>500ms)
curl http://localhost:9200/mcp-audit/_search -d '{
  "query": {
    "range": {"latency_ms": {"gte": 500}}
  }
}'
```

## Troubleshooting

### Tool Not Found

```
Error: TOOL_NOT_FOUND
```

**Solution:**
1. Verify tool is registered: `SELECT * FROM tool_registry WHERE tool_id = 'my.tool';`
2. Check Redis cache: `redis-cli GET tools:my.tool`
3. Verify method format: `api.namespace.tool_id`

### Authorization Denied

```
Error: POLICY_DENIED
```

**Solution:**
1. Check policy: `SELECT * FROM tool_policy WHERE user_id = 'user@company.com' AND tool_id = 'my.tool';`
2. Verify `allowed = true`
3. Clear Redis cache: `redis-cli DEL policy:user@company.com:my.tool`

### Invalid Params

```
Error: INVALID_PARAMS
```

**Solution:**
1. Check schema: `SELECT input_schema FROM tool_registry WHERE tool_id = 'my.tool';`
2. Verify all required fields are present
3. Verify field types match schema

### Kill Switch Active

```
Error: TOOL_DISABLED
```

**Solution:**
1. Check kill switch: `redis-cli GET kill_switch:tool:my.tool`
2. Re-enable: `redis-cli DEL kill_switch:tool:my.tool`

## Next Steps

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) for deep dive
2. Read [README.md](README.md) for complete reference
3. Implement your tools following the pattern above
4. Set up OAuth2 provider for production
5. Configure Elasticsearch for audit retention
6. Set up monitoring and alerting

## Production Checklist

- [ ] OAuth2 provider configured
- [ ] PostgreSQL replicated
- [ ] Redis cluster deployed
- [ ] Elasticsearch cluster deployed
- [ ] SSL/TLS certificates installed
- [ ] Monitoring and alerting configured
- [ ] Audit log retention policy set
- [ ] Backup strategy implemented
- [ ] Load testing completed
- [ ] Security audit completed
