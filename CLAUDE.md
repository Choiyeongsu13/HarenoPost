# CLAUDE.md — ハレの日ポスト

이 파일은 Claude Code가 이 프로젝트에서 작업할 때 먼저 읽는 안내서다. 자세한 내용은 `docs/` 아래 문서를 본다.

## 1. 프로젝트 한 줄 요약

회원이 기념일 **포스트(우체통)** 를 만들어 링크를 공유하면, 누구나 로그인 없이 **카드**를 넣을 수 있고, **주인만 개봉일 이후에** 카드를 열어볼 수 있는 일본어 웹 서비스.

- 기념일(테마) 3종: お正月(1/1 0:00 고정) · 誕生日(회원 생일, 변경 가능) · クリスマス(12/25 0:00 고정)
- 우체통 색은 **빨강 고정**. 테마별 배경 + 상징 장식(지붕 위/앞/옆, 여러 개 선택) + 이름표(8자)
- 화면 언어: **일본어**, 모바일 우선. 문서·주석·커밋 메시지: 한국어

## 2. 반드시 지킬 제약

1. **배운 범위 안에서 구현**: 개인 학습 저장소(Spring_project)에서 쓴 기술만 쓴다. 새 라이브러리(QueryDSL, MapStruct, React, Vue, WebSocket, Redis 등)를 추가하지 않는다. 필요해 보이면 먼저 사람에게 묻는다.
2. 필수 기술: **Spring Security, Spring Data JPA, Thymeleaf, ajax(jQuery)**
3. DB는 **Oracle**, 배포는 **AWS EC2**
4. JPA 연관관계는 `@ManyToOne` 대신 **Long FK 필드**(`memberIdx`, `postIdx` …)로 둔다. 조인이 필요하면 서비스에서 두 번 조회하거나 JPQL을 쓴다.
5. 서버에서 반드시 검사(클라이언트 검사만으로 끝내지 않음):
   - **주인 검사**: 포스트의 `memberIdx` == 로그인 회원
   - **개봉일 검사**: 개봉일 전에는 카드 `contents`가 어떤 응답(HTML, JSON)에도 들어가면 안 된다. 개수만 보여 준다.
   - **도배 방지**: 같은 IP는 1분에 카드 1장
6. 시간대: JVM `-Duser.timezone=Asia/Tokyo`, 서버 OS도 Asia/Tokyo. 날짜는 `LocalDateTime`
7. 비밀 정보(DB 비밀번호, 메일 계정)는 **환경변수** 또는 git에 올리지 않는 `application-secret.yml` 에만 둔다. 저장소에 절대 커밋하지 않는다.

## 3. 기술 스택

| 구분 | 내용 |
|---|---|
| 언어/빌드 | Java 21, Gradle |
| 프레임워크 | Spring Boot 4.0.x |
| 의존성 | `spring-boot-starter-webmvc`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-mail`, `thymeleaf-extras-springsecurity6`, `com.oracle.database.jdbc:ojdbc17`, Lombok, devtools |
| 프론트 | Thymeleaf + jQuery 3.7.1(로컬 파일) + 직접 쓴 CSS. `design/` 의 css/js 를 그대로 `static/` 으로 옮겨 쓴다 |
| DB | Oracle, `sql/schema.sql` 로 직접 생성. 서버에서는 `ddl-auto: none` |

## 4. 패키지 구조

```
com.harenopost
 ├─ HarenoPostApplication.java
 ├─ config      SecurityConfig, WebConfig(업로드 경로 매핑)
 ├─ member      Member, MemberRepository, MemberService, MemberController, MemberApiController, dto/
 ├─ mail        EmailService, MailApiController
 ├─ post        Post, PostRepository, PostService, PostController, PostApiController, Theme(enum), Deco(enum), dto/
 ├─ card        Card, CardRepository, CardService, CardApiController, dto/
 ├─ design      Design, DesignRepository, DesignService
 ├─ report      Report, ReportRepository, ReportService
 ├─ admin       AdminController, AdminApiController
 └─ common      ApiResponse, GlobalExceptionHandler, BusinessException, ErrorCode, SecurityUtil
```

템플릿: `src/main/resources/templates/{index, member/*, post/*, card/*, admin/*, fragments/*}.html`

## 5. 코딩 규칙

- Controller(화면) → `XxxController`, ajax(JSON) → `XxxApiController` (`@RestController`, `/api/**`)
- Entity는 setter 대신 의미 있는 메서드(`post.decorate(...)`, `card.markRead()`)
- 요청/응답은 DTO로 분리하고 `@Valid` 로 검사. Entity를 그대로 JSON으로 내보내지 않는다
- JSON 응답 형식은 `docs/06_api.md` 의 `ApiResponse` 를 따른다
- 서비스 메서드에 `@Transactional`, 조회는 `@Transactional(readOnly = true)`
- 사용자 입력은 Thymeleaf `th:text` 로만 출력(`th:utext` 금지), jQuery는 `.text()` 사용(XSS)
- 모든 POST ajax는 CSRF 헤더를 보낸다(`fragments/head.html` 의 meta + `$.ajaxSetup`)
- 업로드 파일은 외부 절대경로(`app.upload-dir`)에 UUID 파일명으로 저장. `getRealPath` 금지

## 6. 작업 순서

`docs/08_tasks.md` 의 날짜별 체크리스트를 위에서부터 진행한다. 한 단계가 끝나면 그 단계의 **완료 기준**을 확인하고 체크한다.

## 7. 문서 지도

| 파일 | 내용 |
|---|---|
| docs/01_overview.md | 서비스 개요, 용어, 테마·장식 정의 |
| docs/02_requirements.md | 기능/비기능 요구사항 (REQ-F, REQ-NF) |
| docs/03_functional_spec.md | 화면별 기능 명세와 예외 처리 |
| docs/04_usecases.md | 유스케이스 다이어그램, 명세 |
| docs/05_database.md | ERD, 테이블 정의, JPA 매핑 메모 |
| docs/06_api.md | URL 목록(화면/폼/ajax), 응답 형식, 에러 코드 |
| docs/07_screens.md | `design/` 시안 HTML 과 템플릿 대응표 |
| docs/08_tasks.md | 10/12~10/26 일정과 완료 기준 |
| docs/09_deploy.md | EC2(t3.small) 에 Oracle XE(Docker) + 앱 올리기 |
| sql/schema.sql | Oracle DDL |
| design/ | 정적 화면 시안(브라우저로 바로 열림). 레이아웃·CSS·테마 그림의 기준 |
