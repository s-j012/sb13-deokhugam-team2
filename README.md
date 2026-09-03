<div align="center">

# 📚 덕후감

### 책 읽는 즐거움을 공유하고, 지식과 감상을 나누는 독서 커뮤니티

도서 등록부터 리뷰 · 댓글 · 좋아요 · 알림 · 인기 콘텐츠까지  
하나의 흐름으로 연결한 Spring Boot 기반 독서 커뮤니티 서비스입니다.

<br/>

[![CI](https://github.com/sb13-team2/sb13-deokhugam-team2/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/sb13-team2/sb13-deokhugam-team2/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/sb13-team2/sb13-deokhugam-team2/branch/main/graph/badge.svg)](https://codecov.io/gh/sb13-team2/sb13-deokhugam-team2)

<br/>

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-ECS%20%7C%20ECR%20%7C%20RDS%20%7C%20S3-232F3E?logo=amazonwebservices&logoColor=white)

<br/>

[🌐 서비스](http://deokhugam-alb-1560810283.ap-northeast-2.elb.amazonaws.com/)
&nbsp;&nbsp;·&nbsp;&nbsp;
[📑 Swagger UI](http://deokhugam-alb-1560810283.ap-northeast-2.elb.amazonaws.com/swagger-ui/index.html)
&nbsp;&nbsp;·&nbsp;&nbsp;
[💻 GitHub](https://github.com/sb13-team2/sb13-deokhugam-team2)

</div>

---

## 📖 프로젝트 소개

덕후감은 도서를 중심으로 사용자가 감상과 의견을 공유할 수 있는 독서 커뮤니티 서비스입니다.

사용자는 도서를 탐색하거나 직접 등록하고 리뷰를 작성할 수 있으며,  
다른 사용자의 리뷰에 댓글과 좋아요를 남길 수 있습니다.

이러한 사용자 활동은 알림으로 연결되고,  
활동 데이터를 기반으로 일간 · 주간 · 월간 · 역대 인기 콘텐츠를 제공합니다.

### 주요 기능

- OCR 기반 ISBN 자동 추출
- ISBN 기반 도서 정보 자동 조회
- 도서 · 리뷰 · 댓글 CRUD 및 도서 · 리뷰 검색
- 리뷰 좋아요 및 사용자 알림
- 인기 도서 · 인기 리뷰 · 파워 유저 랭킹
- Cursor Pagination 기반 목록 조회
- Soft Delete 기반 데이터 관리
- JaCoCo 커버리지 검증 및 Codecov 리포트 연동
- GitHub Actions 기반 CI
- Docker · AWS ECS 기반 자동 배포

> 프로젝트 기간  
> 2026.08.12 ~ 2026.09.04

---

## 👥 팀원 구성

| 팀원 | GitHub | 담당 도메인 |
|:---:|:---:|:---:|
| 강현구 | [@ssummer96](https://github.com/ssummer96) | User |
| 김도형 | [@DHK777](https://github.com/DHK777) | Dashboard |
| 김지원 | [@dhfqor1101](https://github.com/dhfqor1101) | Review |
| 양성식 | [@seongsik-ai](https://github.com/seongsik-ai) | Comment |
| 이소정 | [@s-j012](https://github.com/s-j012) | Notification |
| 최성웅 | [@sungwoong-svg](https://github.com/sungwoong-svg) | Book |

---

## 🛠 기술 스택

### Backend

![Java](https://img.shields.io/badge/Java%2017-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5.14-6DB33F?logo=springboot&logoColor=white)
![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?logo=spring&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)

- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Scheduler
- MapStruct
- Springdoc OpenAPI / Swagger
- P6Spy
- Lombok

### Database & Test

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white)

- PostgreSQL
- H2 In-memory DB
- JUnit 5
- Mockito
- JaCoCo
- Codecov

### External API

- OCR.Space API
- Kakao Book Search API
- Google Books API

### Infra & CI/CD

![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?logo=amazonwebservices&logoColor=white)

- GitHub Actions
- Docker
- Amazon ECR
- Amazon ECS
- Amazon RDS for PostgreSQL
- Amazon S3
- AWS Application Load Balancer
- AWS OIDC

---

## ✨ 핵심 기능

### 📚 Book

- 도서 등록 · 조회 · 수정 · 논리 삭제
- 제목 · 저자 · ISBN 기반 검색
- 정렬 및 Cursor Pagination
- OCR.Space를 이용한 ISBN-13 자동 추출
- Kakao Book Search API를 이용한 ISBN 기반 도서 정보 조회
- Kakao 썸네일 우선 사용 및 Google Books Thumbnail fallback
- Local Storage / AWS S3 공통 Storage 인터페이스 구성
- 썸네일 교체 시 트랜잭션 커밋 · 롤백에 따른 Storage 파일 정합성 처리

> 초기 요구사항의 Naver Book API가 2026.07.31 서비스 종료됨에 따라  
> 최종 구현에서는 Kakao Book Search API로 대체했습니다.

---

### 📝 Review

- 리뷰 등록 · 상세 조회 · 목록 조회 · 수정 · 논리 삭제
- 작성자 · 도서 · 키워드 검색
- 작성일 · 평점 기준 Cursor Pagination
- 리뷰 좋아요 등록 / 취소
- `PESSIMISTIC_WRITE`를 이용한 좋아요 동시성 제어
- 좋아요 등록 시 리뷰 작성자에게 알림 생성

---

### 💬 Comment

- 댓글 등록 · 조회 · 수정 · 논리 삭제
- 작성자 권한 및 활성 사용자 / 리뷰 검증
- `createdAt + id` 기반 Cursor Pagination
- 사용자 ID를 수집해 `findAllById`로 작성자 일괄 조회
- 사용자 Soft Delete 상태에서도 기존 댓글 작성자 정보 유지
- 댓글 생성 · 삭제 시 리뷰 `commentCount` 정합성 관리
- 다른 사용자의 리뷰에 댓글 작성 시 알림 생성

---

### 🔔 Notification

- 리뷰 좋아요 알림
- 새로운 댓글 알림
- 인기 리뷰 TOP10 알림
- 단일 / 전체 알림 읽음 상태 변경
- Cursor Pagination 기반 알림 목록 조회
- `confirmedAt` 기준 확인 후 7일이 지난 알림 자동 삭제
- Domain Event 기반 Dashboard ↔ Notification 연동
- `AFTER_COMMIT` + `REQUIRES_NEW` 트랜잭션 처리

---

### 🏆 Dashboard

- 인기 도서 랭킹
- 인기 리뷰 랭킹
- 파워 유저 랭킹
- `DAILY · WEEKLY · MONTHLY · ALL_TIME` 기간별 Snapshot 생성
- 점수 계산 및 동일 점수에 대한 ID 기준 정렬 후 순차적인 ranking 부여
- `ranking + createdAt` 기반 Cursor Pagination
- 매일 스케줄러를 통한 랭킹 생성
- 랭킹별 집계 기준에 따라 Soft Delete 데이터 포함 범위 적용

---

### 👤 User

- 회원 등록 · 로그인 · 조회 · 수정
- 사용자 Soft Delete
- 논리 삭제 후 1일 경과 시 연관 데이터와 함께 물리 삭제
- 사용자 상태에 따른 조회 및 활동 데이터 관리

---

## 👨‍💻 팀원별 구현 기능

### 강현구 — User

- 사용자 등록 · 로그인 · 조회 · 수정
- 사용자 Soft Delete
- 논리 삭제 후 1일 경과 시 물리 삭제
- 사용자 연관 데이터 정리
- User 도메인 테스트 및 커버리지 보완

### 김도형 — Dashboard

- 인기 도서 · 인기 리뷰 · 파워 유저 랭킹
- 기간별 Ranking Snapshot 생성
- 랭킹 점수 계산 및 정렬
- Cursor Pagination
- TOP 리뷰 선정 후 Domain Event 발행

### 김지원 — Review

- 리뷰 CRUD · 검색 · Cursor Pagination
- 리뷰 좋아요 Toggle
- 비관적 쓰기 락을 이용한 좋아요 동시성 제어
- 리뷰 좋아요 알림 연동
- Docker 이미지 구성
- AWS ECS 배포 환경 구성
- GitHub Actions CD 및 AWS OIDC 자동 배포 구성

### 양성식 — Comment

- 댓글 CRUD 및 Cursor Pagination
- 댓글 작성자 일괄 조회를 통한 반복 쿼리 개선
- 댓글 생성 · 삭제 시 `commentCount` 정합성 관리
- Soft Delete 상태 사용자의 기존 댓글 작성자 정보 유지
- 댓글 작성 알림 연동

### 이소정 — Notification

- 알림 목록 조회 및 읽음 상태 관리
- 좋아요 · 댓글 · TOP10 리뷰 알림
- 확인 후 7일 경과 알림 자동 정리
- Domain Event Listener
- `AFTER_COMMIT` · `REQUIRES_NEW` 기반 트랜잭션 처리

### 최성웅 — Book

- 도서 CRUD · 검색 · 정렬 · Cursor Pagination
- OCR.Space 기반 ISBN-13 추출
- Kakao Book Search API 기반 도서 정보 조회
- Kakao 원본 썸네일 Base64 변환 및 Google Books Thumbnail fallback
- Local / S3 Storage 추상화
- 썸네일 교체 시 커밋 · 롤백에 따른 파일 정합성 처리
- GitHub Actions CI 테스트 환경 구성
- JaCoCo 전체 프로젝트 **80% Coverage Gate** 구성
- JaCoCo XML Report 및 Codecov 연동

---

## 🔄 주요 처리 흐름

### 1. 도서 정보 자동 조회

```text
도서 이미지
   ↓
OCR.Space
   ↓
ISBN-13 추출
   ↓
Kakao Book Search API
   ↓
도서 정보 조회
   ↓
Kakao Thumbnail
   ↓
원본 이미지 추출 · Base64 변환
   ↓ 썸네일 처리 실패 시
Google Books Thumbnail fallback
```

---

### 2. Ranking Snapshot 생성

```text
Review · Book · User 활동 데이터
              ↓
         기간별 데이터 집계
              ↓
           점수 계산
              ↓
     Score DESC + ID 기준 정렬
              ↓
       순차적 Ranking 부여
              ↓
       Ranking Snapshot 저장
```

지원 기간

```text
DAILY
WEEKLY
MONTHLY
ALL_TIME
```

---

### 3. 인기 리뷰 → 알림

```text
Review Ranking 생성
        ↓
TOP10 Review 선정
        ↓
TopReviewRankedEvent 발행
        ↓
Transaction COMMIT
        ↓
@TransactionalEventListener
        ↓
Notification 생성
```

---

### 4. CI/CD

```text
main 반영
   ↓
GitHub Actions
   ↓
Test
   ↓
JaCoCo Coverage Verification
   ↓
JaCoCo XML Report
   ↓
Codecov Upload
   ↓
Docker Image Build
   ↓
Amazon ECR Push
   ↓
Amazon ECS Deploy
```

---

## ✅ 테스트 및 품질 관리

### CI

`main`, `develop` 브랜치의 Push / Pull Request를 기준으로  
GitHub Actions CI가 실행됩니다.

```bash
./gradlew clean check --no-daemon
```

검증 및 리포트 항목

- 전체 테스트 실행
- JaCoCo Coverage Verification
- **Overall Instruction Coverage 80% 이상**
- JaCoCo XML Report 생성
- Codecov로 커버리지 리포트 업로드
- Codecov를 통한 커버리지 결과 확인

> `application-test.yml`은  
> H2 In-memory DB + PostgreSQL Compatibility Mode로 구성했습니다.

---

## 🚀 배포 구조

```text
                       ┌───────────────┐
                       │    GitHub     │
                       └───────┬───────┘
                               │
                               ▼
                       ┌───────────────┐
                       │ GitHub Actions│
                       └───────┬───────┘
                               │
                               ▼
                       ┌───────────────┐
                       │    Docker     │
                       └───────┬───────┘
                               │
                               ▼
                       ┌───────────────┐
                       │  Amazon ECR   │
                       └───────┬───────┘
                               │
                               ▼
                       ┌───────────────┐
                       │  Amazon ECS   │
                       └───────┬───────┘
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
             ┌─────────────┐       ┌─────────────┐
             │ RDS         │       │ Amazon S3   │
             │ PostgreSQL  │       │ Image       │
             └─────────────┘       └─────────────┘
```

---

## 📁 프로젝트 구조

```text
sb13-deokhugam-team2
├── .github
│   └── workflows
│       ├── ci.yml
│       └── cd.yml
│
├── src
│   ├── main
│   │   ├── java/com/deokhugam
│   │   │   ├── DeokhugamApplication.java
│   │   │   ├── book
│   │   │   ├── comment
│   │   │   ├── dashboard
│   │   │   ├── global
│   │   │   ├── notification
│   │   │   ├── review
│   │   │   └── user
│   │   │
│   │   └── resources
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── static
│   │
│   └── test
│       ├── java/com/deokhugam
│       └── resources
│           └── application-test.yml
│
├── Dockerfile
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🌐 서비스 링크

| 구분 | 링크 |
|---|---|
| 🌐 서비스 | [덕후감 바로가기](http://deokhugam-alb-1560810283.ap-northeast-2.elb.amazonaws.com/) |
| 📑 Swagger | [Swagger UI](http://deokhugam-alb-1560810283.ap-northeast-2.elb.amazonaws.com/swagger-ui/index.html) |
| 📊 Coverage | [Codecov](https://codecov.io/gh/sb13-team2/sb13-deokhugam-team2) |

---

## 📑 프로젝트 문서

| 문서 | 링크 |
|---|---|
| 팀 협업 문서 | [Notion](https://app.notion.com/p/3b9d4721bcfd8156aa36c5cf306afd6b?source=copy_link) |
| 발표 자료 | [발표 자료](https://drive.google.com/file/d/1ZsCBIJCb0fS84MPCkHK5NdZH9dJsivhx/view?usp=drive_link) |
| 시연 영상 | [시연 영상](https://drive.google.com/file/d/1ggF1K1R-k8q6bEeSAd-fzYcJ6aEPPyBa/view?usp=drive_link) |

---

<div align="center">

### 📚 덕후감

책을 읽고, 기록하고, 함께 이야기하는 공간

</div>