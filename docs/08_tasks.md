# 08. 작업 순서 (2026-10-12 ~ 10-26, 하루 3~4시간)

하루 단위로 진행한다. 각 날짜의 **완료 기준**을 직접 확인한 뒤 체크한다. 늦어지면 ⭐(선택) 항목부터 뺀다.

## 10/12 (월) 프로젝트 준비
- [ ] Spring Boot 4.0.x / Java 21 / Gradle 프로젝트 `com.harenopost` 생성, CLAUDE.md 의 의존성 추가
- [ ] `application.yml`(공통), `application-local.yml`, `application-secret.yml`(gitignore) 분리. `.gitignore` 에 secret 파일 추가
- [ ] 로컬 Oracle에 `sql/schema.sql` 실행
- [ ] `design/` 의 css·js를 `static/` 으로 복사, `fragments/head.html`, `fragments/header.html` 작성
- 완료 기준: 앱이 뜨고 `/` 에 시안과 같은 메인 화면이 보인다. DB 연결 오류 없음

## 10/13 (화) 엔티티 · Security 기본
- [ ] User, Post, Card, Design, Report 엔티티 + Repository, `Theme`, `Deco` enum
- [ ] `SecurityConfig`, `UserDetailsService`, `BCryptPasswordEncoder`, 로그인 화면
- [ ] `ApiResponse`, `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`
- 완료 기준: `ddl-auto: validate` 로 기동 성공. 관리자 계정(BCrypt 값 넣은 것)으로 로그인/로그아웃 된다

## 10/14 (수) 회원가입 · 이메일 인증
- [ ] 아이디 중복 확인 ajax
- [ ] `EmailService`(기존 저장소 방식), 인증 코드 세션 저장(5분, 30초 재전송 제한)
- [ ] 가입 POST + 서버 재검사, `/login?joined`
- 완료 기준: 실제 메일로 코드를 받아 가입 → 로그인 성공. 응답 JSON 어디에도 코드가 없다

## 10/15 (목) 마이페이지 · 프로필 수정
- [ ] 마이페이지(프로필, 통계, 포스트 목록)
- [ ] 닉네임·생일 수정, 패스워드 변경, 탈퇴
- [ ] 로그인 성공 시 `last_login` 갱신
- 완료 기준: 수정 내용이 DB와 화면에 반영. 탈퇴 후 같은 ID로 로그인 불가

## 10/16 (금) 포스트 만들기
- [ ] `Theme.nextOpenDate()` (お正月·クリスマス 고정, 誕生日 다음 생일) + 단위 테스트
- [ ] 만들기 화면, POST, 코드 생성, 완료 화면, 受付中 5개 제한
- 완료 기준: 3테마 모두 만들어지고 개봉일이 규칙대로 저장된다. 고정 테마에 다른 날짜를 보내도 무시된다

## 10/17 (토) 포스트 화면
- [ ] `/post/{code}` 배경·장식·이름표·카운트다운·카드 수·봉투
- [ ] 주인/방문자 버튼 분기, 404 화면
- 완료 기준: 로그아웃 상태로도 열린다. 페이지 소스에 카드 본문이 없다

## 10/18 (일) 포스트 꾸미기 · 설정
- [ ] 꾸미기 화면 + `PUT /api/posts/{code}/decorate` (테마 장식 검사, 슬롯 순 CSV)
- [ ] 설정(이름·기념일·개봉일 변경, 기념일 변경 시 장식 초기화), 삭제
- 완료 기준: 다른 사람 포스트의 꾸미기 URL → 403. 다른 테마 장식 코드를 보내면 `INVALID_DECO`

## 10/19 (월) 카드 보내기
- [ ] 디자인 목록 API, 카드 모달, `POST /api/posts/{code}/cards`
- [ ] IP 1분 제한, 개봉 후 수신 마감, 회원 닉네임 자동 입력
- 완료 기준: 새로고침 없이 봉투 추가. 1분 안에 두 번째 전송 → 429 토스트. 응답에 contents 없음

## 10/20 (화) 카드 열어보기 · 신고
- [ ] `/post/{code}/cards` (주인·개봉 후), 모달, 읽음 처리
- [ ] 신고 ajax, 중복 신고 방지
- 완료 기준: DB에서 open_date를 과거로 바꿔 열람 확인. 개봉 전 접근은 포스트 화면으로 돌아간다

## 10/21 (수) 관리자
- [ ] 신고 목록·처리·카드 삭제
- [ ] 디자인 업로드(외부 경로, UUID 파일명, 2MB, 확장자 검사), 사용 여부·순서, 삭제
- 완료 기준: 업로드한 디자인이 카드 모달에 나타나고, 사용 안 함으로 바꾸면 사라진다. 일반 회원은 `/admin` 403

## 10/22 (목) 다듬기
- [ ] 입력 검증 메시지 일본어 통일, 에러 페이지
- [ ] 스마트폰 폭(375px)에서 모든 화면 확인
- [ ] ⭐ 카드 디자인 기본 이미지 테마별 2장씩 등록
- 완료 기준: 07_screens.md 의 모든 화면이 시안과 같은 모습

## 10/23 (금) EC2 배포 준비
- [ ] EC2 생성, Java 21 설치, 시간대 `sudo timedatectl set-timezone Asia/Tokyo`
- [ ] Oracle 준비: t3.small 에 Docker Oracle XE (`docs/09_deploy.md`), `schema.sql` 실행, 관리자 계정 BCrypt 값 입력
- [ ] 업로드 디렉터리 생성, 환경변수로 DB·메일 정보 설정
- 완료 기준: EC2에서 `date` 가 JST, DB 접속 확인

## 10/24 (토) 배포
- [ ] `./gradlew bootJar` → 업로드 → `nohup java -Xmx512m -Duser.timezone=Asia/Tokyo -jar ... --spring.profiles.active=prod &` (09_deploy.md 6번)
- [ ] 보안 그룹 포트, (⭐) 80 → 8080 포워딩
- 완료 기준: 휴대폰으로 외부 접속해 가입~카드 보내기까지 된다

## 10/25 (일) 통합 테스트
- [ ] 아래 시나리오를 배포 서버에서 실행
  1. 가입 → 포스트 3종 생성 → 꾸미기 → 링크를 다른 브라우저(비로그인)로 열어 카드 3장
  2. 같은 IP 연속 전송 차단 확인
  3. DB에서 open_date 과거로 → 열람, 읽음, 신고 → 관리자 처리
  4. 다른 계정으로 남의 `/post/{code}/cards`, `/decorate` 접근 → 403
- 완료 기준: 모든 시나리오 통과, 오류 로그 없음

## 10/26 (월) 마무리
- [ ] README(실행 방법, 화면 캡처), 발표 자료
- [ ] 저장소에 비밀 정보가 없는지 최종 확인(`git log -p | grep -i password`)
- 완료 기준: 제출
