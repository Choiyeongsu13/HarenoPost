# 06. URL / API

URL·템플릿 이름은 학습 저장소 **exSampleJPA 형태**를 따른다.

- 컨트롤러마다 `@RequestMapping("User")` 처럼 폴더 이름을 붙이고, 주소는 `user_insert` 처럼 **소문자_밑줄**
- 같은 주소를 GET(폼) / POST(처리)로 나눠 쓴다. 메서드 이름은 폼 `userInsert`, 처리 `userInsertPro`
- 템플릿은 `User/user_insert.html` 처럼 **폴더 대문자 / 파일 소문자_밑줄**
- 공유 링크만 짧게 `/post/{code}` (→ `/Post/post_view?code=` 로 forward)

권한: `ALL` 누구나, `USER` 로그인 회원, `OWNER` 포스트 주인(서비스에서 검사), `ADMIN` ROLE_ADMIN

## 1. 화면 (GET)

| URL | 권한 | 템플릿 | 설명 |
|---|---|---|---|
| `/` | ALL | index | 메인(가운데 우체통) |
| `/post/{code}` | ALL | (forward) | 공유 링크 → `/Post/post_view?code=` |
| `/User/user_login` | ALL | User/user_login | `?error`, `?joined` 메시지 |
| `/User/user_insert` | ALL | User/user_insert | 회원가입 폼 |
| `/User/user_mypage` | USER | User/user_mypage | |
| `/User/user_modify` | USER | User/user_modify | 프로필·비밀번호·탈퇴 |
| `/Post/post_write` | USER | Post/post_write | 포스트 만들기 |
| `/Post/post_created?code=` | OWNER | Post/post_created | 완료 화면(링크 복사) |
| `/Post/post_view?code=` | ALL | Post/post_view | 주인이면 버튼 추가. 본문 없음 |
| `/Post/post_decorate?code=` | OWNER, 개봉 전 | Post/post_decorate | |
| `/Post/post_modify?code=` | OWNER, 개봉 전 | Post/post_modify | 이름·기념일·개봉일 + 삭제 |
| `/Card/card_list?code=` | OWNER, 개봉 후 | Card/card_list | (10/20) 개봉 전이면 `/post/{code}` 로 redirect |
| `/Admin/admin_reports` | ADMIN | Admin/admin_reports | (10/21) `?status=WAIT\|DONE` |
| `/Admin/admin_designs` | ADMIN | Admin/admin_designs | (10/21) |
| `/upload/**` | ALL | — | 업로드 이미지(외부 경로 매핑) |

## 2. 폼 전송 (POST, redirect)

| URL | 권한 | 성공 시 |
|---|---|---|
| `/User/user_login` | ALL | Security 처리 → `/User/user_mypage` (또는 원래 가려던 화면) |
| `/User/user_logout` | USER | `/` |
| `/User/user_insert` | ALL | `/User/user_login?joined` |
| `/User/user_modify` | USER | `/User/user_mypage` |
| `/User/user_passwd` | USER | `/User/user_mypage` |
| `/User/user_delete` | USER | 로그아웃 후 `/` |
| `/Post/post_write` | USER | `/Post/post_created?code=` |
| `/Post/post_modify?code=` | OWNER | `/post/{code}` |
| `/Post/post_delete` (code) | OWNER | `/User/user_mypage` |
| `/Admin/admin_designs` (multipart) | ADMIN | (10/21) `/Admin/admin_designs` |

- 입력 DTO: 가입 `UserRequestDTO`, 프로필 `UserModifyRequestDTO`, 포스트 `PostRequestDTO` (`@Valid`)
- 비밀번호 변경·탈퇴는 `@RequestParam` 으로 받고 서비스에서 검사한다
- 검증 실패는 같은 화면을 다시 보여 주고 `BindingResult` 메시지(또는 `passwdError`, `deleteError`)를 출력한다

## 3. ajax (POST, 문자열 응답)

exSampleJPA 의 `user_idCheck` 처럼 **문자열**을 돌려준다. JSON 은 쓰지 않는다.

- 성공: `200` + 메시지 문자열 (ID 중복검사만 `"true"`/`"false"`)
- 실패: ErrorCode 의 상태 코드(400, 403, 409, 429 …) + 일본어 메시지 문자열
- 화면 JS 는 `success: function (result) { ... }`, `error: function (xhr) { errMsg(xhr) }` (`Include/head.html` 의 `errMsg` 가 `xhr.responseText` 를 꺼낸다)

| URL | 권한 | 요청 파라미터 | 성공 응답 |
|---|---|---|---|
| `/User/user_idCheck` | ALL | `userid` | `"true"`(이미 사용 중) / `"false"` |
| `/User/user_mailSend` | ALL | `email` | `認証コードを送信しました（5分間有効）` ← 코드는 응답에 넣지 않는다 |
| `/User/user_mailCheck` | ALL | `email`, `code` | `メール認証が完了しました` |
| `/Post/post_decorate` | OWNER, 개봉 전 | `code`, `decos`(여러 개), `nameTag` | `保存しました` |
| `/Card/card_write` | ALL, 개봉 전 | `code`, `designIdx`, `senderName`, `contents` | (10/19) `カードを届けました！` ← contents 는 돌려주지 않는다. 봉투는 화면에서 입력값으로 추가 |
| `/Card/card_read` | OWNER, 개봉 후 | `cardIdx` | (10/20) `OK` |
| `/Card/card_report` | OWNER, 개봉 후 | `cardIdx`, `reason` | (10/20) `通報しました` |
| `/Admin/admin_reportDone` | ADMIN | `reportIdx` | (10/21) `処理済みにしました` |
| `/Admin/admin_cardDelete` | ADMIN | `cardIdx` | (10/21) `削除しました` |
| `/Admin/admin_designModify` | ADMIN | `designIdx`, `useYn`, `sortOrder` | (10/21) `保存しました` |
| `/Admin/admin_designDelete` | ADMIN | `designIdx` | (10/21) `削除しました` |

- 카드 디자인 목록은 ajax 로 따로 받지 않고 포스트 화면 모델(`dList`)에 넣어 그린다 (10/19)
- jQuery 배열 파라미터는 `traditional: true` 로 보낸다 (`decos=KAGAMI&decos=KADOMATSU`)

## 4. 에러 처리

`BusinessException(ErrorCode)` 를 던지면 `GlobalExceptionHandler` 가

- ajax 요청(`X-Requested-With: XMLHttpRequest`) → 상태 코드 + 메시지 문자열
- 화면 요청 → **에러 팝업**(`error/403`, `error/404`, `error/error` → `Include/error_popup :: popup`, 「OK」 로 이전 화면)

으로 바꾼다. 그 밖의 예외도 같은 방식(500)으로 처리하고, 내부 메시지는 로그에만 남긴다.

### ErrorCode

| 코드 | HTTP | 메시지 |
|---|---|---|
| `INVALID_INPUT` | 400 | 入力内容を確認してください |
| `INTERNAL_ERROR` | 500 | エラーが発生しました。しばらくしてからもう一度お試しください |
| `DUPLICATE_USERID` | 409 | このIDはすでに使われています |
| `DUPLICATE_EMAIL` | 409 | このメールアドレスはすでに登録されています |
| `USERID_NOT_CHECKED` | 400 | IDの重複確認をしてください |
| `MAIL_NOT_VERIFIED` | 400 | メール認証を完了してください |
| `MAIL_SEND_FAILED` | 500 | メールを送信できませんでした |
| `MAIL_TOO_FAST` | 429 | しばらくしてからもう一度お試しください |
| `MAIL_CODE_WRONG` | 400 | 認証コードが正しくありません |
| `MAIL_CODE_EXPIRED` | 400 | 認証コードの有効期限が切れました。もう一度送信してください |
| `INVALID_EMAIL` | 400 | メールアドレスの形式を確認してください |
| `PASSWORD_WRONG` | 400 | パスワードが正しくありません |
| `PASSWORD_FORMAT` | 400 | パスワードは英字と数字を含む 8〜20文字で入力してください |
| `PASSWORD_MISMATCH` | 400 | パスワードが一致しません |
| `USER_NOT_FOUND` | 404 | 会員が見つかりません |
| `POST_NOT_FOUND` | 404 | ポストが見つかりません |
| `POST_LIMIT` | 400 | 同時につくれるポストは5つまでです |
| `OPEN_DATE_REQUIRED` | 400 | 開封日時を選んでください |
| `OPEN_DATE_PAST` | 400 | 開封日は今より後の日時を選んでください |
| `OPEN_DATE_TOO_FAR` | 400 | 開封日は1年以内で選んでください |
| `NOT_OWNER` | 403 | アクセス権がありません |
| `ALREADY_OPENED` | 400 | 開封後は変更できません |
| `NOT_OPENED_YET` | 400 | まだ開封日ではありません |
| `RECEIVE_CLOSED` | 400 | このポストはもう受付を終了しました |
| `CARD_TOO_FAST` | 429 | 少し時間をおいてから送ってください（1分に1通まで） |
| `INVALID_DECO` | 400 | 選べない飾りが含まれています |
| `NAME_TAG_TOO_LONG` | 400 | 名札は8文字までです |
| `ALREADY_REPORTED` | 409 | すでに通報済みです |
| `LOGIN_REQUIRED` | 401 | ログインしてください |

## 5. Security 설정 요약

```java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/User/user_login", "/User/user_insert", "/User/user_idCheck",
                "/User/user_mailSend", "/User/user_mailCheck").permitAll()
        .requestMatchers("/post/*", "/Post/post_view").permitAll()
        .requestMatchers("/css/**", "/js/**", "/img/**", "/upload/**", "/error", "/error/**", "/favicon.ico").permitAll()
        .requestMatchers("/Admin/**").hasRole("ADMIN")
        .anyRequest().authenticated())
    .formLogin(login -> login.loginPage("/User/user_login").loginProcessingUrl("/User/user_login")
        .usernameParameter("userid").passwordParameter("passwd")
        .successHandler(loginSuccessHandler).failureUrl("/User/user_login?error").permitAll())
    .logout(logout -> logout.logoutUrl("/User/user_logout").logoutSuccessUrl("/"))
    // ajax 는 401, 그 밖의 화면은 로그인 화면으로
    .exceptionHandling(...)
```

- 10/19 카드 보내기 때 `/Card/card_write` 를 permitAll 에 추가한다 (비로그인도 보낼 수 있음)
- CSRF 는 켜 둔다. `Include/head.html` 의 meta + `$.ajaxSetup` 이 모든 ajax 에 CSRF 헤더를 붙인다
