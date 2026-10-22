package com.harenopost;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.harenopost.config.LoginSuccessHandler;
import com.harenopost.config.SecurityConfig;
import com.harenopost.dto.CustomUserDetails;
import com.harenopost.dto.EnvelopeResponseDTO;
import com.harenopost.dto.PostResponseDTO;
import com.harenopost.dto.Theme;
import com.harenopost.dto.UserResponseDTO;
import com.harenopost.entity.PostEntity;
import com.harenopost.entity.UserEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.service.CardService;
import com.harenopost.service.DesignService;
import com.harenopost.service.MailAuthService;
import com.harenopost.service.PostService;
import com.harenopost.service.ReportService;
import com.harenopost.service.UserService;
import com.harenopost.dto.CardResponseDTO;
import com.harenopost.dto.Role;
import com.harenopost.entity.CardEntity;

//DB 없이 화면(Thymeleaf)과 보안 설정을 확인한다. 서비스는 가짜(mock)
@WebMvcTest
@Import({ SecurityConfig.class, LoginSuccessHandler.class })
class WebPageTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	UserService userService;
	@MockitoBean
	PostService postService;
	@MockitoBean
	CardService cardService;
	@MockitoBean
	MailAuthService mailAuthService;
	@MockitoBean
	DesignService designService;
	@MockitoBean
	ReportService reportService;

	CustomUserDetails ownerUser;
	CustomUserDetails otherUser;
	PostEntity postEntity;

	@BeforeEach
	void setUp() throws Exception {
		UserEntity owner = UserEntity.builder().userid("yusuke01").passwd("{noop}x").nickname("ゆうすけ")
				.email("y@example.com").birthday(LocalDate.of(2000, 11, 20)).build();
		setField(owner, "userIdx", 1L);
		setField(owner, "regdate", LocalDateTime.of(2026, 10, 12, 10, 0));
		ownerUser = new CustomUserDetails(owner);

		UserEntity other = UserEntity.builder().userid("other01").passwd("{noop}x").nickname("ほか")
				.email("o@example.com").build();
		setField(other, "userIdx", 2L);
		otherUser = new CustomUserDetails(other);

		postEntity = PostEntity.builder().code("abcd1234").userIdx(1L).title("ゆうすけのポスト").theme(Theme.SHOGATSU)
				.openDate(LocalDateTime.now().plusDays(30)).nameTag("ゆうすけ").build();
		setField(postEntity, "postIdx", 10L);

		given(userService.userView(1L)).willReturn(new UserResponseDTO(owner));
		given(postService.postView(eq("abcd1234"), any()))
				.willAnswer(inv -> new PostResponseDTO(postEntity, inv.getArgument(1), 1));
		given(postService.postView(eq("nothing0"), any())).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));
		given(postService.postOwnerView("abcd1234", 1L)).willReturn(new PostResponseDTO(postEntity, 1L, 0));
		given(postService.postModify("abcd1234", 1L)).willReturn(new PostResponseDTO(postEntity, 1L, 0));
		given(postService.postModify("abcd1234", 2L)).willThrow(new BusinessException(ErrorCode.NOT_OWNER));
		given(postService.defaultOpenDate(any(), any())).willReturn(LocalDateTime.of(2027, 1, 1, 0, 0));
		given(cardService.envelopeList(10L)).willReturn(List.of(new EnvelopeResponseDTO(1L, "たろう", null, false)));
	}

	@Test
	void 메인_로그인_가입_화면() throws Exception {
		mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("ポストをつくる")));
		mvc.perform(get("/User/user_login")).andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")));
		mvc.perform(get("/User/user_insert")).andExpect(status().isOk()).andExpect(content().string(containsString("重複確認")))
			//JS 가 결과를 넣는 자리 (th:errors 는 에러가 없으면 태그를 지우므로 쓰면 안 된다)
			.andExpect(content().string(containsString("id=\"idMsg\"")))
			.andExpect(content().string(containsString("id=\"pwMsg\"")))
			.andExpect(content().string(containsString("id=\"mailMsg\"")));
	}

	@Test
	void 가입_검증에_걸려도_ID_메일_확인_상태는_유지된다() throws Exception {
		given(userService.userIdCheck("newuser1")).willReturn(false); //사용 가능
		given(mailAuthService.isVerified(any(), eq("a@example.com"))).willReturn(true);
		mvc.perform(post("/User/user_insert").with(csrf())
				.param("userid", "newuser1").param("passwd", "short").param("passwdConfirm", "short")
				.param("nickname", "user").param("email", "a@example.com"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("パスワードは英字と数字を含む 8〜20文字で入力してください")))
			.andExpect(content().string(containsString("var idOk = true;")))
			.andExpect(content().string(containsString("var mailOk = true;")));
	}

	@Test
	void 공유_링크는_포스트_화면으로_넘긴다() throws Exception {
		mvc.perform(get("/post/abcd1234")).andExpect(forwardedUrl("/Post/post_view?code=abcd1234"));
	}

	@Test
	void 포스트_화면은_비로그인도_열리고_본문이_없다() throws Exception {
		mvc.perform(get("/Post/post_view").param("code", "abcd1234"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ゆうすけのポスト")))
			.andExpect(content().string(containsString("カードを書く")))
			.andExpect(content().string(containsString("たろう")))
			.andExpect(content().string(containsString("+09:00")))
			.andExpect(content().string(not(containsString("data-contents")))) //본문 데이터 없음
			.andExpect(content().string(not(containsString("post_decorate"))));
	}

	@Test
	void 주인에게는_꾸미기_설정_버튼() throws Exception {
		mvc.perform(get("/Post/post_view").param("code", "abcd1234").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("/Post/post_decorate?code=abcd1234")))
			.andExpect(content().string(containsString("/Post/post_modify?code=abcd1234")));
	}

	@Test
	void 없는_포스트는_404_팝업() throws Exception {
		mvc.perform(get("/Post/post_view").param("code", "nothing0"))
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("ポストが見つかりません")))
			.andExpect(content().string(containsString("btnBack")));
	}

	@Test
	void 에러는_팝업으로_내부_메시지는_숨긴다() throws Exception {
		given(postService.postView(eq("boom0000"), any())).willThrow(new IllegalStateException("DB-INTERNAL-DETAIL"));
		mvc.perform(get("/Post/post_view").param("code", "boom0000"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(containsString("btnBack")))
			.andExpect(content().string(containsString("しばらくしてからもう一度お試しください")))
			.andExpect(content().string(not(containsString("DB-INTERNAL-DETAIL"))));
		//ajax 는 메시지 문자열
		given(postService.postDecorate(eq("boom0000"), anyLong(), any(), any())).willThrow(new IllegalStateException("DB-INTERNAL-DETAIL"));
		mvc.perform(post("/Post/post_decorate").with(csrf()).with(user(ownerUser)).header("X-Requested-With", "XMLHttpRequest")
				.param("code", "boom0000"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().string(containsString("しばらくしてからもう一度お試しください")))
			.andExpect(content().string(not(containsString("DB-INTERNAL-DETAIL"))));
	}

	@Test
	void 로그인이_필요한_화면은_로그인으로() throws Exception {
		mvc.perform(get("/Post/post_write")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/User/user_login"));
		mvc.perform(get("/User/user_mypage")).andExpect(redirectedUrl("/User/user_login"));
		mvc.perform(get("/Post/post_decorate").param("code", "abcd1234")).andExpect(redirectedUrl("/User/user_login"));
	}

	@Test
	void 로그인_회원_화면() throws Exception {
		given(postService.postList(1L)).willReturn(List.of(new PostResponseDTO(postEntity, 1L, 6)));
		mvc.perform(get("/User/user_mypage").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ゆうすけのポスト")))
			.andExpect(content().string(containsString("受付中")))
			.andExpect(content().string(containsString("6通")))
			.andExpect(content().string(containsString("11月20日")));
		mvc.perform(get("/User/user_modify").with(user(ownerUser))).andExpect(status().isOk())
			.andExpect(content().string(containsString("id=\"mailMsg\"")));
		mvc.perform(get("/Post/post_write").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ゆうすけのポスト")))
			.andExpect(content().string(containsString("2027-01-01")));
		mvc.perform(get("/Post/post_created").param("code", "abcd1234").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("/post/abcd1234")));
		mvc.perform(get("/Post/post_decorate").param("code", "abcd1234").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("KAGAMI")));
		mvc.perform(get("/Post/post_modify").param("code", "abcd1234").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ポストを削除")));
	}

	@Test
	void 남의_포스트_꾸미기는_403() throws Exception {
		mvc.perform(get("/Post/post_decorate").param("code", "abcd1234").with(user(otherUser)))
			.andExpect(status().isForbidden())
			.andExpect(content().string(containsString("アクセス権がありません")));
	}

	@Test
	void 꾸미기_ajax() throws Exception {
		//비로그인 ajax → 401
		mvc.perform(post("/Post/post_decorate").with(csrf()).header("X-Requested-With", "XMLHttpRequest")
				.param("code", "abcd1234").param("decos", "KAGAMI"))
			.andExpect(status().isUnauthorized());
		//성공 → 메시지 문자열
		mvc.perform(post("/Post/post_decorate").with(csrf()).with(user(ownerUser)).header("X-Requested-With", "XMLHttpRequest")
				.param("code", "abcd1234").param("decos", "KAGAMI", "KADOMATSU").param("nameTag", "ゆうすけ"))
			.andExpect(status().isOk())
			.andExpect(content().string("保存しました"));
		//다른 테마 장식 → 400 + 메시지
		given(postService.postDecorate(eq("abcd1234"), anyLong(), anyList(), any())).willThrow(new BusinessException(ErrorCode.INVALID_DECO));
		mvc.perform(post("/Post/post_decorate").with(csrf()).with(user(ownerUser)).header("X-Requested-With", "XMLHttpRequest")
				.param("code", "abcd1234").param("decos", "SANTA"))
			.andExpect(status().isBadRequest())
			.andExpect(content().string("選べない飾りが含まれています"));
		//CSRF 없으면 거절
		mvc.perform(post("/Post/post_decorate").with(user(ownerUser)).param("code", "abcd1234"))
			.andExpect(status().isForbidden());
	}

	@Test
	void 아이디_중복_확인_ajax() throws Exception {
		given(userService.userIdCheck(anyString())).willReturn(false);
		given(userService.userIdCheck("admin")).willReturn(true);
		mvc.perform(post("/User/user_idCheck").with(csrf()).param("userid", "newuser1"))
			.andExpect(status().isOk())
			.andExpect(content().string("false"));
		mvc.perform(post("/User/user_idCheck").with(csrf()).param("userid", "admin"))
			.andExpect(content().string("true"));
	}

	@Test
	void 메일_전송_ajax는_코드를_응답에_넣지_않는다() throws Exception {
		mvc.perform(post("/User/user_mailSend").with(csrf()).header("X-Requested-With", "XMLHttpRequest")
				.param("email", "a@example.com"))
			.andExpect(status().isOk())
			.andExpect(content().string("認証コードを送信しました（5分間有効）"));
		//서비스가 거절하면 상태 코드 + 메시지
		willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL)).given(userService).userMailSend(any(), eq("dup@example.com"));
		mvc.perform(post("/User/user_mailSend").with(csrf()).header("X-Requested-With", "XMLHttpRequest")
				.param("email", "dup@example.com"))
			.andExpect(status().isConflict())
			.andExpect(content().string("このメールアドレスはすでに登録されています"));
	}

	@Test
	void 카드_보내기_ajax는_비로그인도_되고_본문을_돌려주지_않는다() throws Exception {
		mvc.perform(post("/Card/card_write").with(csrf()).header("X-Requested-With", "XMLHttpRequest")
				.param("code", "abcd1234").param("senderName", "たろう").param("contents", "SECRET-BODY"))
			.andExpect(status().isOk())
			.andExpect(content().string("カードを届けました！"));
		//1분 안에 다시 → 429 + 메시지
		willThrow(new BusinessException(ErrorCode.CARD_TOO_FAST)).given(cardService).cardWrite(any(), anyString());
		mvc.perform(post("/Card/card_write").with(csrf()).header("X-Requested-With", "XMLHttpRequest")
				.param("code", "abcd1234").param("senderName", "たろう").param("contents", "2通目"))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().string("少し時間をおいてから送ってください（1分に1通まで）"));
	}

	@Test
	void 포스트_화면에_카드_모달과_닉네임() throws Exception {
		UserEntity other = UserEntity.builder().userid("other01").passwd("x").nickname("ほか").email("o@example.com").build();
		given(userService.userView(2L)).willReturn(new UserResponseDTO(other));
		mvc.perform(get("/Post/post_view").param("code", "abcd1234").with(user(otherUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("id=\"writeModal\"")))
			.andExpect(content().string(containsString("投函する")))
			//로그인 회원이면 이름 칸에 닉네임이 미리 들어간다
			.andExpect(content().string(containsString("value=\"ほか\"")));
	}

	@Test
	void 카드_목록은_개봉_전이면_포스트_화면으로() throws Exception {
		given(cardService.cardList("abcd1234", 1L)).willThrow(new BusinessException(ErrorCode.NOT_OPENED_YET));
		mvc.perform(get("/Card/card_list").param("code", "abcd1234").with(user(ownerUser)))
			.andExpect(redirectedUrl("/post/abcd1234"));
	}

	@Test
	void 카드_목록은_주인_개봉_후에_본문을_보여_준다() throws Exception {
		CardEntity card = CardEntity.builder().postIdx(10L).senderName("たろう").contents("おめでとう<script>").ip("1.1.1.1").build();
		setField(card, "cardIdx", 5L);
		given(cardService.cardList("abcd1234", 1L)).willReturn(List.of(new CardResponseDTO(card, null)));
		mvc.perform(get("/Card/card_list").param("code", "abcd1234").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("たろう")))
			//우체통(장식·이름표)도 보여 준다
			.andExpect(content().string(containsString("class=\"mailbox-mini\"")))
			.andExpect(content().string(containsString("\"KAGAMI\"")))
			//본문은 이스케이프되어 data 속성에 들어간다
			.andExpect(content().string(containsString("おめでとう&lt;script&gt;")))
			.andExpect(content().string(not(containsString("おめでとう<script>"))));
		//남의 카드 목록 → 403
		given(cardService.cardList("abcd1234", 2L)).willThrow(new BusinessException(ErrorCode.NOT_OWNER));
		mvc.perform(get("/Card/card_list").param("code", "abcd1234").with(user(otherUser)))
			.andExpect(status().isForbidden());
	}

	@Test
	void 관리자_화면은_ADMIN만() throws Exception {
		mvc.perform(get("/Admin/admin_reports").with(user(ownerUser))).andExpect(status().isForbidden());
		UserEntity admin = UserEntity.builder().userid("admin").passwd("{noop}x").nickname("管理者").email("a@a.jp").build();
		setField(admin, "userIdx", 9L);
		setField(admin, "role", Role.ROLE_ADMIN);
		CustomUserDetails adminUser = new CustomUserDetails(admin);
		mvc.perform(get("/Admin/admin_reports").with(user(adminUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("通報はありません")));
		mvc.perform(get("/Admin/admin_designs").with(user(adminUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("enctype=\"multipart/form-data\"")));
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field f = target.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(target, value);
	}
}
