# MCP Spoke Server SDK - Complete Index

## 📚 Documentation (Start Here)

1. **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Overview of what was generated
   - What was delivered
   - Key features
   - Architecture highlights
   - Getting started

2. **[README.md](README.md)** - Complete reference guide (1000+ lines)
   - Architecture overview
   - Execution flow (12 phases)
   - Security model (6 layers)
   - API reference
   - Configuration
   - Troubleshooting

3. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Deep dive into system design (1000+ lines)
   - System architecture diagram
   - Request processing pipeline
   - Error handling strategy
   - Caching strategy
   - Audit logging architecture
   - Performance characteristics
   - Deployment topology

4. **[QUICKSTART.md](QUICKSTART.md)** - Local development setup (500+ lines)
   - Prerequisites
   - Docker Compose setup
   - Database initialization
   - Building and running
   - Testing examples
   - Implementing your own tools
   - Troubleshooting

5. **[DELIVERY_CHECKLIST.md](DELIVERY_CHECKLIST.md)** - What was delivered
   - Complete checklist of all requirements
   - Verification of all features
   - Quality metrics

## 🏗️ Project Structure

### Core SDK Modules

```
mcp-core/                          # JSON-RPC 2.0 engine & enforcement
├── rpc/                           # Dispatcher, request/response models
├── validation/                    # JSON Schema validator
├── registry/                      # Tool registry service
├── policy/                        # Authorization & RBAC/ABAC
├── killswitch/                    # Kill switch enforcement
├── masking/                       # Column-level data masking
├── audit/                         # Audit logging service
├── error/                         # Standard error codes
└── meta/                          # MCP metadata model

mcp-rest-adapter/                  # REST → JSON-RPC protocol adapter
├── controller/                    # REST endpoints
└── converter/                     # Protocol conversion

mcp-security/                      # OAuth2 JWT validation
├── jwt/                           # JWT token validation
└── oauth/                         # OAuth2 configuration

mcp-redis/                         # Redis cache implementations
├── RedisToolRegistryRepository
├── RedisKillSwitchRepository
└── RedisPolicyRepository

mcp-postgres/                      # PostgreSQL repositories
├── entity/                        # JPA entities
└── repository/                    # JPA repositories

mcp-elasticsearch/                 # Elasticsearch audit logging
└── ElasticsearchAuditRepository

mcp-autoconfigure/                 # Spring Boot auto-configuration
└── McpServerAutoConfiguration
```

### Sample Application

```
sample-spoke-app/                  # Complete example implementation
├── SampleSpokeApplication         # Main class
├── tool/
│   └── Ifrs17LossProjectionHandler # Example tool handler
├── config/
│   └── SampleToolRegistry         # Tool registration
└── resources/
    ├── application.yml            # Development config
    └── application-prod.yml       # Production config
```

## 🔑 Key Components

### JSON-RPC 2.0 Dispatcher

**File:** `mcp-core/src/main/java/com/financial/mcp/core/rpc/JsonRpcDispatcher.java`

The heart of the system. Implements the 12-phase validation pipeline:
1. JSON Parsing
2. JSON-RPC Structure Validation
3. Meta Field Validation
4. Global Kill Switch Check
5. Tool Registry Lookup
6. Tool Kill Switch Check
7. Authorization Validation
8. Input Schema Validation
9. Handler Execution
10. Data Masking
11. Async Audit Logging
12. Response Serialization

### REST Adapter

**Files:**
- `mcp-rest-adapter/src/main/java/com/financial/mcp/rest/controller/RestAdapterController.java`
- `mcp-rest-adapter/src/main/java/com/financial/mcp/rest/converter/RestToJsonRpcConverter.java`

Converts REST requests to JSON-RPC internally. All business logic remains in the dispatcher.

### Tool Registry

**Files:**
- `mcp-core/src/main/java/com/financial/mcp/core/registry/ToolRegistryService.java`
- `mcp-postgres/src/main/java/com/financial/mcp/postgres/repository/PostgresToolRegistryRepository.java`
- `mcp-redis/src/main/java/com/financial/mcp/redis/RedisToolRegistryRepository.java`

Manages tool registration with PostgreSQL master and Redis cache (60-minute TTL).

### Authorization & Policy

**Files:**
- `mcp-core/src/main/java/com/financial/mcp/core/policy/PolicyService.java`
- `mcp-postgres/src/main/java/com/financial/mcp/postgres/repository/PostgresPolicyRepository.java`
- `mcp-redis/src/main/java/com/financial/mcp/redis/RedisPolicyRepository.java`

RBAC + ABAC enforcement with Redis caching (30-minute TTL).

### Kill Switch

**Files:**
- `mcp-core/src/main/java/com/financial/mcp/core/killswitch/KillSwitchService.java`
- `mcp-redis/src/main/java/com/financial/mcp/redis/RedisKillSwitchRepository.java`

Tool-level and global disable with immediate effect.

### Data Masking

**File:** `mcp-core/src/main/java/com/financial/mcp/core/masking/DataMaskingService.java`

Column-level masking with three types:
- HASH: SHA-256 (first 16 chars)
- REDACT: ***REDACTED***
- PARTIAL: Show 25%, mask rest

### Audit Logging

**Files:**
- `mcp-core/src/main/java/com/financial/mcp/core/audit/AuditService.java`
- `mcp-elasticsearch/src/main/java/com/financial/mcp/elasticsearch/ElasticsearchAuditRepository.java`

Asynchronous audit logging to Elasticsearch with SHA-256 hashing of params.

## 📋 Database Schema

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

## 🔐 Security Layers

1. **Transport**: HTTPS/TLS
2. **Authentication**: OAuth2 JWT with JWKS
3. **Authorization**: RBAC + ABAC with policy caching
4. **Data Protection**: Column-level masking
5. **Audit**: Server-side logging with SHA-256 hashing
6. **Operational**: Kill switch enforcement

## 📊 Caching Strategy

| Cache | Key Pattern | TTL | Fallback |
|-------|-------------|-----|----------|
| Tool Registry | `tools:{tool_id}` | 60 min | PostgreSQL |
| Tool Policy | `policy:{user_id}:{tool_id}` | 30 min | PostgreSQL |
| Masking Policy | `masking:{user_id}:{tool_id}` | 30 min | PostgreSQL |
| Kill Switch | `kill_switch:tool:{tool_id}` | None | Persistent |
| Kill Switch | `kill_switch:global` | None | Persistent |

## 🚀 Quick Start

### 1. Local Development

```bash
# Start infrastructure
docker-compose up -d

# Initialize database
psql -h localhost -U postgres -d mcp_db < schema.sql

# Build
./gradlew clean build

# Run
./gradlew :sample-spoke-app:bootRun
```

### 2. Test JSON-RPC

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3},
    "id": "uuid-v7",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

### 3. Test REST

```bash
curl -X POST http://localhost:8080/api/ifrs17/loss-projection \
  -H "Content-Type: application/json" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid" \
  -H "X-Dept: RISK" \
  -d '{"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3}'
```

## 📖 Reading Guide

### For Architects
1. Start with [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) for deep dive
3. Review [README.md](README.md) for complete reference

### For Developers
1. Start with [QUICKSTART.md](QUICKSTART.md)
2. Review sample-spoke-app for examples
3. Read [README.md](README.md) for API reference
4. Check [ARCHITECTURE.md](ARCHITECTURE.md) for details

### For DevOps
1. Read [QUICKSTART.md](QUICKSTART.md) for local setup
2. Review application-prod.yml for production config
3. Check [README.md](README.md) for deployment section
4. Review [ARCHITECTURE.md](ARCHITECTURE.md) for topology

### For Security
1. Read [README.md](README.md) Security Model section
2. Review [ARCHITECTURE.md](ARCHITECTURE.md) Security Layers section
3. Check [QUICKSTART.md](QUICKSTART.md) for testing authorization

## 🎯 Key Features

✅ **Spoke-Only MCP**: No hub dependency
✅ **Dual Interface**: JSON-RPC 2.0 + REST
✅ **Single Logic Core**: No duplication
✅ **Financial-Grade Security**: OAuth2, RBAC, ABAC, masking
✅ **Audit Compliance**: Server-side logging, Elasticsearch
✅ **Kill Switch**: Tool-level & global disable
✅ **Caching**: Redis with PostgreSQL fallback
✅ **Async Logging**: Non-blocking audit trail
✅ **Error Handling**: Standard error codes
✅ **Validation**: JSON Schema + meta validation
✅ **Data Protection**: Column-level masking
✅ **Trace Correlation**: Distributed tracing support
✅ **Production Ready**: Monitoring, metrics, health checks

## 📞 Support

### Common Issues

**Tool Not Found**
- Check: `SELECT * FROM tool_registry WHERE tool_id = 'my.tool';`
- Clear cache: `redis-cli DEL tools:my.tool`

**Authorization Denied**
- Check: `SELECT * FROM tool_policy WHERE user_id = 'user@company.com' AND tool_id = 'my.tool';`
- Verify: `allowed = true`

**Kill Switch Active**
- Check: `redis-cli GET kill_switch:tool:my.tool`
- Re-enable: `redis-cli DEL kill_switch:tool:my.tool`

**Audit Logs Missing**
- Verify Elasticsearch: `curl http://localhost:9200/mcp-audit/_search`
- Check async executor: `@EnableAsync` on main class

### Documentation References

- Architecture questions → [ARCHITECTURE.md](ARCHITECTURE.md)
- API reference → [README.md](README.md)
- Setup issues → [QUICKSTART.md](QUICKSTART.md)
- Feature verification → [DELIVERY_CHECKLIST.md](DELIVERY_CHECKLIST.md)

## 🔗 File Navigation

### Core Implementation
- [JsonRpcDispatcher](mcp-core/src/main/java/com/financial/mcp/core/rpc/JsonRpcDispatcher.java) - Main dispatcher
- [RestAdapterController](mcp-rest-adapter/src/main/java/com/financial/mcp/rest/controller/RestAdapterController.java) - REST endpoints
- [ToolRegistryService](mcp-core/src/main/java/com/financial/mcp/core/registry/ToolRegistryService.java) - Tool management
- [PolicyService](mcp-core/src/main/java/com/financial/mcp/core/policy/PolicyService.java) - Authorization
- [KillSwitchService](mcp-core/src/main/java/com/financial/mcp/core/killswitch/KillSwitchService.java) - Kill switch
- [DataMaskingService](mcp-core/src/main/java/com/financial/mcp/core/masking/DataMaskingService.java) - Data masking
- [AuditService](mcp-core/src/main/java/com/financial/mcp/core/audit/AuditService.java) - Audit logging

### Sample Application
- [SampleSpokeApplication](sample-spoke-app/src/main/java/com/financial/mcp/sample/SampleSpokeApplication.java) - Main class
- [Ifrs17LossProjectionHandler](sample-spoke-app/src/main/java/com/financial/mcp/sample/tool/Ifrs17LossProjectionHandler.java) - Example tool
- [SampleToolRegistry](sample-spoke-app/src/main/java/com/financial/mcp/sample/config/SampleToolRegistry.java) - Tool registration

### Configuration
- [application.yml](sample-spoke-app/src/main/resources/application.yml) - Development config
- [application-prod.yml](sample-spoke-app/src/main/resources/application-prod.yml) - Production config

## ✅ Status

**Project Status: COMPLETE AND PRODUCTION-READY**

All requirements met:
- ✅ 7 SDK modules
- ✅ 1 sample spoke application
- ✅ 40+ Java classes
- ✅ 4 comprehensive documentation files
- ✅ Complete Spring Boot starter framework
- ✅ Production-ready code

**Ready for:**
- ✅ Local development
- ✅ Integration testing
- ✅ Production deployment
- ✅ Custom tool implementation
- ✅ Enterprise adoption
