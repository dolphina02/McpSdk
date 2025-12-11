# MCP Spoke Server SDK - Documentation

<div align="center">

**Languages:** [🇬🇧 English](README.md) | [🇰🇷 한글](README_KO.md)

</div>

Complete documentation for the MCP Spoke Server SDK in English and Korean.

## 🎯 What is This SDK?

**MCP Spoke Server SDK** is a **Spring Boot Starter library** for building **MCP Spoke Servers** - the server-side component that exposes tools/capabilities to LLM agents via the Model Context Protocol.

### Key Points
- **Purpose**: Build production-grade MCP servers with Spring Boot 3.x
- **MCP Layer**: Implements the **MCP Server (Spoke) Layer** - the server-side component
- **Dual Interface**: JSON-RPC 2.0 (native MCP protocol) + REST (for API gateways)
- **Single Core**: All business logic in JSON-RPC dispatcher (no duplication)
- **Financial-Grade**: Security, audit, compliance built-in

### Architecture Layer
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
        └────────────────────────┘
```

### What You Build
- **MCP Spoke Servers**: Standalone servers exposing tools to LLM agents
- **Tool Implementations**: Custom business logic wrapped as MCP tools
- **Secure APIs**: With OAuth2, RBAC, ABAC, and audit logging
- **Production-Ready**: With caching, kill switches, and monitoring

## 📚 Documentation

### Core Documentation
- **[README](mcp-server-sdk/docs/en/README.md)** - Complete reference guide (1000+ lines)
- **[INDEX](mcp-server-sdk/docs/en/INDEX.md)** - Complete index and navigation guide
- **[PROJECT_SUMMARY](mcp-server-sdk/docs/en/PROJECT_SUMMARY.md)** - Overview of what was generated
- **[ARCHITECTURE](mcp-server-sdk/docs/en/ARCHITECTURE.md)** - Deep dive into system design
- **[DELIVERY_CHECKLIST](mcp-server-sdk/docs/en/DELIVERY_CHECKLIST.md)** - Verification of all features
- **[FEATURES.md](mcp-server-sdk/FEATURES.md)** - Complete feature documentation

### Guides
- **[QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md)** - Local development setup
- **[DEV_SETUP](mcp-server-sdk/docs/guides/en/DEV_SETUP.md)** - Development environment configuration
- **[BUILD_CHECK](mcp-server-sdk/docs/guides/en/BUILD_CHECK.md)** - Build environment verification
- **[BUILD_ENVIRONMENT_SUMMARY](mcp-server-sdk/docs/guides/en/BUILD_ENVIRONMENT_SUMMARY.md)** - Build environment summary

## 🎯 Quick Navigation

### For Architects
1. Start with [PROJECT_SUMMARY](mcp-server-sdk/docs/en/PROJECT_SUMMARY.md)
2. Read [ARCHITECTURE](mcp-server-sdk/docs/en/ARCHITECTURE.md) for deep dive
3. Review [README](mcp-server-sdk/docs/en/README.md) for complete reference

### For Developers
1. Start with [QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md)
2. Review sample-spoke-app for examples
3. Read [README](mcp-server-sdk/docs/en/README.md) for API reference
4. Check [ARCHITECTURE](mcp-server-sdk/docs/en/ARCHITECTURE.md) for details

### For DevOps
1. Read [QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md) for local setup
2. Review application-prod.yml for production config
3. Check [README](mcp-server-sdk/docs/en/README.md) for deployment section
4. Review [ARCHITECTURE](mcp-server-sdk/docs/en/ARCHITECTURE.md) for topology

### For Security
1. Read [README](mcp-server-sdk/docs/en/README.md) Security Model section
2. Review [ARCHITECTURE](mcp-server-sdk/docs/en/ARCHITECTURE.md) Security Layers section
3. Check [QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md) for testing authorization

## 📋 Documentation Structure

```
docs/
├── en/                          # English documentation
│   ├── README.md               # Complete reference
│   ├── INDEX.md                # Navigation guide
│   ├── PROJECT_SUMMARY.md      # Overview
│   ├── ARCHITECTURE.md         # Deep dive
│   └── DELIVERY_CHECKLIST.md   # Feature verification
├── ko/                          # Korean documentation
│   ├── README.md               # 완전한 참고
│   ├── INDEX.md                # 네비게이션 가이드
│   ├── PROJECT_SUMMARY.md      # 개요
│   ├── ARCHITECTURE.md         # 심화
│   └── DELIVERY_CHECKLIST.md   # 기능 검증
├── guides/
│   ├── en/                     # English guides
│   │   ├── QUICKSTART.md       # Local setup
│   │   ├── DEV_SETUP.md        # Dev environment
│   │   ├── BUILD_CHECK.md      # Build verification
│   │   └── BUILD_ENVIRONMENT_SUMMARY.md
│   └── ko/                     # Korean guides
│       ├── QUICKSTART.md       # 로컬 설정
│       ├── DEV_SETUP.md        # 개발 환경
│       ├── BUILD_CHECK.md      # 빌드 확인
│       └── BUILD_ENVIRONMENT_SUMMARY.md
└── README.md                   # Documentation index
```

## 🔑 Key Features

See [FEATURES.md](FEATURES.md) for complete feature documentation.

**Security**: OAuth2/JWT, RBAC/ABAC, Data Masking
**Reliability**: Idempotency, Kill Switches, Tool Versioning
**Observability**: Audit Logging, Trace Correlation, Health Checks
**Performance**: Caching (Redis + PostgreSQL), JSON Schema Validation, Error Handling

## 🚀 Getting Started

1. Read [QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md)
2. Follow the setup steps
3. Test the API
4. Implement your tools

## 📞 Support

For issues or questions:
1. Check the relevant documentation
2. Review the examples in sample-spoke-app
3. Check troubleshooting sections

## 📄 License

Proprietary - Financial Grade MCP SDK
