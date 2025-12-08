# MCP Spoke Server SDK - Documentation

Complete documentation for the MCP Spoke Server SDK in English and Korean.

## 📚 English Documentation

### Core Documentation
- **[README](en/README.md)** - Complete reference guide (1000+ lines)
- **[INDEX](en/INDEX.md)** - Complete index and navigation guide
- **[PROJECT_SUMMARY](en/PROJECT_SUMMARY.md)** - Overview of what was generated
- **[ARCHITECTURE](en/ARCHITECTURE.md)** - Deep dive into system design
- **[DELIVERY_CHECKLIST](en/DELIVERY_CHECKLIST.md)** - Verification of all features

### Guides
- **[QUICKSTART](guides/en/QUICKSTART.md)** - Local development setup
- **[DEV_SETUP](guides/en/DEV_SETUP.md)** - Development environment configuration
- **[BUILD_CHECK](guides/en/BUILD_CHECK.md)** - Build environment verification
- **[BUILD_ENVIRONMENT_SUMMARY](guides/en/BUILD_ENVIRONMENT_SUMMARY.md)** - Build environment summary

## 📚 한글 문서

### 핵심 문서
- **[README](ko/README.md)** - 완전한 참고 가이드 (1000+ 줄)
- **[INDEX](ko/INDEX.md)** - 완전한 인덱스 및 네비게이션 가이드
- **[PROJECT_SUMMARY](ko/PROJECT_SUMMARY.md)** - 생성된 내용 개요
- **[ARCHITECTURE](ko/ARCHITECTURE.md)** - 시스템 설계 심화
- **[DELIVERY_CHECKLIST](ko/DELIVERY_CHECKLIST.md)** - 모든 기능 검증

### 가이드
- **[QUICKSTART](guides/ko/QUICKSTART.md)** - 로컬 개발 환경 설정
- **[DEV_SETUP](guides/ko/DEV_SETUP.md)** - 개발 환경 설정
- **[BUILD_CHECK](guides/ko/BUILD_CHECK.md)** - 빌드 환경 확인
- **[BUILD_ENVIRONMENT_SUMMARY](guides/ko/BUILD_ENVIRONMENT_SUMMARY.md)** - 빌드 환경 요약

## 🎯 Quick Navigation

### For Architects
1. Start with [PROJECT_SUMMARY](en/PROJECT_SUMMARY.md)
2. Read [ARCHITECTURE](en/ARCHITECTURE.md) for deep dive
3. Review [README](en/README.md) for complete reference

### For Developers
1. Start with [QUICKSTART](guides/en/QUICKSTART.md)
2. Review sample-spoke-app for examples
3. Read [README](en/README.md) for API reference
4. Check [ARCHITECTURE](en/ARCHITECTURE.md) for details

### For DevOps
1. Read [QUICKSTART](guides/en/QUICKSTART.md) for local setup
2. Review application-prod.yml for production config
3. Check [README](en/README.md) for deployment section
4. Review [ARCHITECTURE](en/ARCHITECTURE.md) for topology

### For Security
1. Read [README](en/README.md) Security Model section
2. Review [ARCHITECTURE](en/ARCHITECTURE.md) Security Layers section
3. Check [QUICKSTART](guides/en/QUICKSTART.md) for testing authorization

## 📖 Reading Guide

### 아키텍트를 위해
1. [PROJECT_SUMMARY](ko/PROJECT_SUMMARY.md)부터 시작
2. [ARCHITECTURE](ko/ARCHITECTURE.md) 읽기
3. [README](ko/README.md) 참고

### 개발자를 위해
1. [QUICKSTART](guides/ko/QUICKSTART.md)부터 시작
2. sample-spoke-app 예제 검토
3. [README](ko/README.md) API 참고
4. [ARCHITECTURE](ko/ARCHITECTURE.md) 상세 확인

### DevOps를 위해
1. [QUICKSTART](guides/ko/QUICKSTART.md) 로컬 설정
2. application-prod.yml 프로덕션 설정 검토
3. [README](ko/README.md) 배포 섹션
4. [ARCHITECTURE](ko/ARCHITECTURE.md) 토폴로지 검토

### 보안 담당자를 위해
1. [README](ko/README.md) 보안 모델 섹션
2. [ARCHITECTURE](ko/ARCHITECTURE.md) 보안 레이어 섹션
3. [QUICKSTART](guides/ko/QUICKSTART.md) 권한 테스트

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
1. Read [QUICKSTART](guides/en/QUICKSTART.md)
2. Follow the setup steps
3. Test the API
4. Implement your tools

### Korean
1. [QUICKSTART](guides/ko/QUICKSTART.md) 읽기
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

