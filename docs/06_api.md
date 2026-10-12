# 06. URL / API

권한: `ALL` 누구나, `USER` 로그인 회원, `OWNER` 포스트 주인(서비스에서 검사), `ADMIN` ROLE_ADMIN

## 1. 화면 (Thymeleaf, GET)

| URL | 권한 | 템플릿 | 설명 |
|---|---|---|---|
| `/` | ALL | index | 메인(가운데 우체통) |
| `/login` | ALL | member/login | `?error`, `?joined`, `?logout` 메시지 |
| `/signup` | ALL | member/signup | |
| `/mypage` | USER | member/mypage | |
| `/mypage/edit` | USER | member/edit | |
| `/post/new` | USER | post/create | |
| `/post/{code}/created` | OWNER | post/created | 완료 화면(링크 복사) |
| `/post/{code}` | ALL | post/view | 주인이면 버튼 추가. 본문 없음 |
| `/post/{code}/decorate` | OWNER, 개봉 전 | post/decorate | |
| `/post/{code}/edit` | OWNER, 개봉 전 | post/edit | |
| `/post/{code}/cards` | OWNER, 개봉 후 | card/list | 개봉 전이면 `/post/{code}` 로 redirect |
| `/admin/reports` | ADMIN | admin/reports | `?status=WAIT\|DONE` |
| `/admin/designs` | ADMIN | admin/designs | |
| `/upload/**` | ALL | — | 업로드 이미지(외부 경로 매핑) |

## 2. 폼 전송 (POST, redirect)

| URL | 권한 | 성공 시 |
|---|---|---|
| `/login` | ALL | Security 처리 |
| `/logout` | USER | `/` |
| `/signup` | ALL | `/login?joined` |
| `/mypage/edit` | USER | `/mypage` |
| `/mypage/password` | USER | `/mypage` |
| `/mypage/withdraw` | USER | 로그아웃 후 `/` |
| `/post/new` | USER | `/post/{code}/created` |
| `/post/{code}/edit` | OWNER | `/post/{code}` |
| `/post/{code}/delete` | OWNER | `/mypage` |
| `/admin/designs` (multipart) | ADMIN | `/admin/designs` |

폼 검증 실패는 같은 화면을 다시 보여 주고 `BindingResult` 메시지를 출력한다.

## 3. ajax (JSON, `/api/**`)

| 메서드 URL | 권한 | 요청 | 성공 data |
|---|---|---|---|
| `GET /api/members/check-id?userid=` | ALL | | `{available: true}` |
| `POST /api/mail/send` | ALL | `{email}` | null |
| `POST /api/mail/verify` | ALL | `{email, code}` | null |
| `PUT /api/posts/{code}/decorate` | OWNER | `{decos: ["KAGAMI","KADOMATSU"], nameTag}` | `{decos, nameTag}` |
| `GET /api/posts/{code}/designs` | ALL | | `[{designIdx, name, imageUrl}]` |
| `POST /api/posts/{code}/cards` | ALL | `{designIdx, senderName, contents}` | `{cardIdx, senderName, imageUrl, count}` ← contents 없음 |
| `POST /api/cards/{idx}/read` | OWNER, 개봉 후 | | null |
| `POST /api/cards/{idx}/report` | OWNER, 개봉 후 | `{reason}` | null |
| `POST /api/admin/reports/{idx}/done` | ADMIN | | null |
| `DELETE /api/admin/cards/{idx}` | ADMIN | | null |
| `PUT /api/admin/designs/{idx}` | ADMIN | `{useYn, sortOrder}` | null |
| `DELETE /api/admin/designs/{idx}` | ADMIN | | null |

### 응답 형식

```json
{ "success": true,  "message": "保存しました", "data": { } }
{ "success": false, "code": "CARD_TOO_FAST", "message": "少し時間をおいてから送ってください（1分に1通まで）", "data": null }
```

```java
public record ApiResponse<T>(boolean success, String code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) { ... }
    public static <T> ApiResponse<T> ok(String message, T data) { ... }
    public static ApiResponse<Void> fail(ErrorCode e) { ... }
}
```

`BusinessException(ErrorCode)` 를 던지고 `GlobalExceptionHandler` 가 `/api/**` 요청이면 JSON, 화면 요청이면 에러 페이지(`error/403`, `error/404`)로 바꾼다.

### ErrorCode

| 코드 | HTTP | 메시지 |
|---|---|---|
| `INVALID_INPUT` | 400 | (검증 메시지) |
| `DUPLICATE_USERID` | 409 | このIDはすでに使われています |
| `DUPLICATE_EMAIL` | 409 | このメールアドレスはすでに登録されています |
| `MAIL_SEND_FAILED` | 500 | メールを送信できませんでした |
| `MAIL_TOO_FAST` | 429 | しばらくしてからもう一度お試しください |
| `MAIL_CODE_WRONG` | 400 | 認証コードが正しくありません |
| `MAIL_CODE_EXPIRED` | 400 | 認証コードの有効期限が切れました。もう一度送信してください |
| `POST_NOT_FOUND` | 404 | ポストが見つかりません |
| `POST_LIMIT` | 400 | 同時につくれるポストは5つまでです |
| `NOT_OWNER` | 403 | アクセス権がありません |
| `ALREADY_OPENED` | 400 | 開封後は変更できません |
| `NOT_OPENED_YET` | 400 | まだ開封日ではありません |
| `RECEIVE_CLOSED` | 400 | このポストはもう受付を終了しました |
| `CARD_TOO_FAST` | 429 | 少し時間をおいてから送ってください（1分に1通まで） |
| `INVALID_DECO` | 400 | 選べない飾りが含まれています |
| `ALREADY_REPORTED` | 409 | すでに通報済みです |
| `LOGIN_REQUIRED` | 401 | ログインしてください |

## 4. Security 설정 요약

```java
http
  .authorizeHttpRequests(a -> a
      .requestMatchers("/", "/login", "/signup", "/post/*", "/css/**", "/js/**", "/img/**", "/upload/**", "/error/**").permitAll()
      .requestMatchers("/api/members/check-id", "/api/mail/**").permitAll()
      .requestMatchers(HttpMethod.GET, "/api/posts/*/designs").permitAll()
      .requestMatchers(HttpMethod.POST, "/api/posts/*/cards").permitAll()
      .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
      .anyRequest().authenticated())
  .formLogin(f -> f.loginPage("/login").usernameParameter("userid").passwordParameter("passwd")
      .successHandler(loginSuccessHandler).failureUrl("/login?error").permitAll())
  .logout(l -> l.logoutSuccessUrl("/"))
  .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(   // /api/** 는 401 JSON
      new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), PathPatternRequestMatcher.withDefaults().matcher("/api/**")));
```

- `/post/{code}` 는 permitAll, 그 아래(`/post/{code}/decorate` 등)는 authenticated + 서비스에서 주인 검사
- CSRF는 켜 둔다. `fragments/head.html`:

```html
<meta name="_csrf" th:content="${_csrf.token}">
<meta name="_csrf_header" th:content="${_csrf.headerName}">
<script>
$.ajaxSetup({ beforeSend: function (xhr) {
  xhr.setRequestHeader($('meta[name=_csrf_header]').attr('content'), $('meta[name=_csrf]').attr('content'));
}});
</script>
```

- ajax는 `contentType: 'application/json'`, `data: JSON.stringify(...)`, 실패 시 `xhr.responseJSON.message` 를 `toast()` 로 표시
