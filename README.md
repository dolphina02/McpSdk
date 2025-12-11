# MCP Spoke Server SDK - Documentation

<div align="center">

**Languages:** [🇬🇧 English](#english-documentation) | [🇰🇷 한글](#한글-문서)

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

---

## 🎯 이 SDK는 무엇인가?

**MCP Spoke Server SDK**는 **MCP Spoke 서버**를 구축하기 위한 **Spring Boot Starter 라이브러리**입니다. MCP Spoke 서버는 LLM 에이전트에 도구/기능을 노출하는 서버 측 컴포넌트입니다.

### 주요 포인트
- **목적**: Spring Boot 3.x로 프로덕션급 MCP 서버 구축
- **MCP 레이어**: **MCP 서버 (Spoke) 레이어** 구현 - 서버 측 컴포넌트
- **이중 인터페이스**: JSON-RPC 2.0 (네이티브 MCP 프로토콜) + REST (API 게이트웨이용)
- **단일 코어**: 모든 비즈니스 로직이 JSON-RPC 디스패처에 있음 (중복 없음)
- **금융 등급**: 보안, 감사, 준수 내장

### 아키텍처 레이어
```
┌─────────────────────────────────────────────────────────┐
│  LLM / AI 에이전트 / 클라이언트 애플리케이션                 │
└────────────────────┬────────────────────────────────────┘
                     │ (JSON-RPC 2.0 또는 REST)
                     │
        ┌────────────▼────────────┐
        │  MCP Spoke 서버          │  ◄── 이 SDK
        │  (당신의 구현)            │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │  당신의 비즈니스 로직      │
        │  (도구/기능)             │
        └────────────────────────┘
```

### 구축하는 것
- **MCP Spoke 서버**: LLM 에이전트에 도구를 노출하는 독립형 서버
- **도구 구현**: MCP 도구로 래핑된 커스텀 비즈니스 로직
- **보안 API**: OAuth2, RBAC, ABAC, 감사 로깅 포함
- **프로덕션 준비**: 캐싱, 킬 스위치, 모니터링 포함

---

## 📚 English Documentation {#english-documentation}

### Core Documentation
- **[README](mcp-server-sdk/docs/en/README.md)** - Complete reference guide (1000+ lines)
- **[INDEX](mcp-server-sdk/docs/en/INDEX.md)** - Complete index and navigation guide
- **[PROJECT_SUMMARY](mcp-server-sdk/docs/en/PROJECT_SUMMARY.md)** - Overview of what was generated
- **[ARCHITECTURE](mcp-server-sdk/docs/en/ARCHITECTURE.md)** - Deep dive into system design
- **[DELIVERY_CHECKLIST](mcp-server-sdk/docs/en/DELIVERY_CHECKLIST.md)** - Verification of all features
- **[FEATURES](mcp-server-sdk/FEATURES.md)** - Complete feature documentation

### Guides
- **[QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md)** - Local development setup
- **[DEV_SETUP](mcp-server-sdk/docs/guides/en/DEV_SETUP.md)** - Development environment configuration
- **[BUILD_CHECK](mcp-server-sdk/docs/guides/en/BUILD_CHECK.md)** - Build environment verification
- **[BUILD_ENVIRONMENT_SUMMARY](mcp-server-sdk/docs/guides/en/BUILD_ENVIRONMENT_SUMMARY.md)** - Build environment summary

---

## 📚 한글 문서 {#한글-문서}

### 핵심 문서
- **[README](mcp-server-sdk/docs/ko/README.md)** - 완전한 참고 가이드 (1000+ 줄)
- **[INDEX](mcp-server-sdk/docs/ko/INDEX.md)** - 완전한 인덱스 및 네비게이션 가이드
- **[PROJECT_SUMMARY](mcp-server-sdk/docs/ko/PROJECT_SUMMARY.md)** - 생성된 내용 개요
- **[ARCHITECTURE](mcp-server-sdk/docs/ko/ARCHITECTURE.md)** - 시스템 설계 심화
- **[DELIVERY_CHECKLIST](mcp-server-sdk/docs/ko/DELIVERY_CHECKLIST.md)** - 모든 기능 검증
- **[FEATURES](mcp-server-sdk/FEATURES.md)** - 완전한 기능 문서

### 가이드
- **[QUICKSTART](mcp-server-sdk/docs/guides/ko/QUICKSTART.md)** - 로컬 개발 환경 설정
- **[DEV_SETUP](mcp-server-sdk/docs/guides/ko/DEV_SETUP.md)** - 개발 환경 설정
- **[BUILD_CHECK](mcp-server-sdk/docs/guides/ko/BUILD_CHECK.md)** - 빌드 환경 확인
- **[BUILD_ENVIRONMENT_SUMMARY](mcp-server-sdk/docs/guides/ko/BUILD_ENVIRONMENT_SUMMARY.md)** - 빌드 환경 요약

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

## 📖 Reading Guide

### 아키텍트용
1. [PROJECT_SUMMARY](mcp-server-sdk/docs/ko/PROJECT_SUMMARY.md)부터 시작
2. [ARCHITECTURE](mcp-server-sdk/docs/ko/ARCHITECTURE.md) 읽기
3. [README](mcp-server-sdk/docs/ko/README.md) 참고

### 개발자용
1. [QUICKSTART](mcp-server-sdk/docs/guides/ko/QUICKSTART.md)부터 시작
2. sample-spoke-app 예제 검토
3. [README](mcp-server-sdk/docs/ko/README.md) API 참고
4. [ARCHITECTURE](mcp-server-sdk/docs/ko/ARCHITECTURE.md) 상세 확인

### DevOps용
1. [QUICKSTART](mcp-server-sdk/docs/guides/ko/QUICKSTART.md) 로컬 설정
2. application-prod.yml 프로덕션 설정 검토
3. [README](mcp-server-sdk/docs/ko/README.md) 배포 섹션
4. [ARCHITECTURE](mcp-server-sdk/docs/ko/ARCHITECTURE.md) 토폴로지 검토

### 보안 담당자용
1. [README](mcp-server-sdk/docs/ko/README.md) 보안 모델 섹션
2. [ARCHITECTURE](mcp-server-sdk/docs/ko/ARCHITECTURE.md) 보안 레이어 섹션
3. [QUICKSTART](mcp-server-sdk/docs/guides/ko/QUICKSTART.md) 권한 테스트

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
└── README.md                   # This file
```

## 🔑 Key Features

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

## 🚀 Getting Started

### English
1. Read [QUICKSTART](mcp-server-sdk/docs/guides/en/QUICKSTART.md)
2. Follow the setup steps
3. Test the API
4. Implement your tools

### Korean
1. [QUICKSTART](mcp-server-sdk/docs/guides/ko/QUICKSTART.md) 읽기
2. 설정 단계 따라하기
3. API 테스트
4. 도구 구현

## 📞 Support

For issues or questions:
1. Check the relevant documentation
2. Review the examples in sample-spoke-app
3. Check troubleshooting sections

## 📄 License

Proprietary - Financial Grade MCP SDK

