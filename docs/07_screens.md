# 07. 화면 시안 → 템플릿 대응

`design/` 의 HTML은 서버 없이 브라우저로 바로 열리는 정적 시안이다(데모 데이터). **레이아웃, 클래스 이름, 문구, CSS, 테마 그림은 시안을 그대로 따른다.** 데모용 스크립트(가짜 데이터, `setTimeout` 등)는 실제 ajax로 바꾼다.

![메인](images/index_shot.png)

## 정적 파일 옮기기

| 시안 | 프로젝트 |
|---|---|
| design/css/common.css | src/main/resources/static/css/common.css |
| design/js/common.js | static/js/common.js (toast, 모달, 링크 복사, 카운트다운) |
| design/js/theme-art.js | static/js/theme-art.js (테마·장식·배경 SVG) |
| design/js/jquery-3.7.1.min.js | static/js/jquery-3.7.1.min.js |

## 화면 대응표

| 시안 | 템플릿 | 바꿀 점 |
|---|---|---|
| index.html | index.html | 로그인 여부로 헤더 버튼 분기(`sec:authorize`) |
| login.html | user/login.html | `th:action="@{/login}"`, name=`userid`/`passwd`, `?error` 메시지 |
| signup.html | user/signup.html | 데모 코드 123456, `admin` 중복 데모 → `/api/users/check-id`, `/api/mail/*` |
| mypage.html | user/mypage.html | 프로필·통계·포스트 목록을 모델로. 상태 배지 受付中/開封済み |
| (없음) | user/edit.html | signup 폼 스타일 재사용 |
| create.html (formArea) | post/create.html | 폼 POST `/post/new`. 테마 값은 hidden `theme` |
| create.html (doneArea) | post/created.html | 링크 `https://{host}/post/{code}` |
| decorate.html | post/decorate.html | 상단 테마 탭 삭제(포스트 테마 고정). 저장 → `PUT /api/posts/{code}/decorate` |
| post.html | post/view.html | `?theme`, `?view=owner` 데모 파라미터 → 모델 값. 카드 모달 → `POST /api/posts/{code}/cards` |
| cards.html | card/list.html | 본문은 서버 렌더링. 읽음/신고 ajax |
| (없음) | post/edit.html | 이름·기념일·개봉일 + 삭제 모달 |
| (없음) | admin/reports.html, admin/designs.html | `panel`, `btn` 클래스로 간단한 표 |
| (없음) | error/403.html, error/404.html | 가운데 우체통 + 문구 |
| screens.html | — | 발표용 목록. 프로젝트에는 넣지 않음 |

## 테마 그림 사용법

```html
<body th:class="|theme-${#strings.toLowerCase(post.theme)}|">
<div class="bg" id="bg"></div>
<div class="post" id="post"> ... </div>
<script th:inline="javascript">
  const theme = /*[[${#strings.toLowerCase(post.theme)}]]*/ 'shogatsu';
  const decos = /*[[${post.decoCodes}]]*/ [];      // ["KAGAMI","KADOMATSU"]
  drawBackground($('#bg'), theme);
  drawDecos($('#post'), decos);
</script>
```

- 카운트다운: `<span class="countdown" th:attr="data-open-at=${post.openDateIso}">` (ISO 문자열, `+09:00` 포함)
- 사용자 입력(이름, 메시지, 이름표)은 `th:text` 만 사용
