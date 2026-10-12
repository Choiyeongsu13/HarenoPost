package com.harenopost;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.harenopost.card.CardService;
import com.harenopost.card.dto.EnvelopeDto;
import com.harenopost.common.BusinessException;
import com.harenopost.common.ErrorCode;
import com.harenopost.config.LoginSuccessHandler;
import com.harenopost.config.SecurityConfig;
import com.harenopost.mail.MailAuthService;
import com.harenopost.member.CustomUserDetails;
import com.harenopost.member.Member;
import com.harenopost.member.MemberRepository;
import com.harenopost.member.MemberService;
import com.harenopost.post.Post;
import com.harenopost.post.PostService;
import com.harenopost.post.Theme;
import com.harenopost.post.dto.MyPostDto;

/**
 * DB 없이 화면(Thymeleaf)과 보안 설정을 확인한다. 서비스는 가짜(mock).
 */
@WebMvcTest
@Import({ SecurityConfig.class, LoginSuccessHandler.class })
class WebPageTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean
	MemberService memberService;
	@MockitoBean
	PostService postService;
	@MockitoBean
	CardService cardService;
	@MockitoBean
	MailAuthService mailAuthService;
	@MockitoBean
	MemberRepository memberRepository;

	Member owner;
	CustomUserDetails ownerUser;
	CustomUserDetails otherUser;
	Post post;

	@BeforeEach
	void setUp() throws Exception {
		owner = Member.create("yusuke01", "{noop}x", "ゆうすけ", "y@example.com", LocalDate.of(2000, 11, 20));
		setField(owner, "memberIdx", 1L);
		setField(owner, "regdate", LocalDateTime.of(2026, 10, 12, 10, 0));
		ownerUser = new CustomUserDetails(owner);

		Member other = Member.create("other01", "{noop}x", "ほか", "o@example.com", null);
		setField(other, "memberIdx", 2L);
		otherUser = new CustomUserDetails(other);

		post = Post.create("abcd1234", 1L, "ゆうすけのポスト", Theme.SHOGATSU, LocalDateTime.now().plusDays(30), "ゆうすけ");
		setField(post, "postIdx", 10L);

		given(memberService.get(1L)).willReturn(owner);
		given(postService.getByCode("abcd1234")).willReturn(post);
		given(postService.getByCode("nothing0")).willThrow(new BusinessException(ErrorCode.POST_NOT_FOUND));
		given(postService.getOwned("abcd1234", 1L)).willReturn(post);
		given(postService.getOwnedBeforeOpen("abcd1234", 1L)).willReturn(post);
		given(postService.getOwnedBeforeOpen("abcd1234", 2L)).willThrow(new BusinessException(ErrorCode.NOT_OWNER));
		given(postService.defaultOpenDate(any(), any())).willReturn(LocalDateTime.of(2027, 1, 1, 0, 0));
		given(cardService.getEnvelopes(10L)).willReturn(List.of(new EnvelopeDto(1L, "たろう", null, false)));
	}

	@Test
	void 메인_로그인_가입_화면() throws Exception {
		mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("ポストをつくる")));
		mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")));
		mvc.perform(get("/signup")).andExpect(status().isOk()).andExpect(content().string(containsString("重複確認")));
	}

	@Test
	void 포스트_화면은_비로그인도_열리고_본문이_없다() throws Exception {
		mvc.perform(get("/post/abcd1234"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ゆうすけのポスト")))
			.andExpect(content().string(containsString("カードを書く")))
			.andExpect(content().string(containsString("たろう")))
			.andExpect(content().string(containsString("+09:00")))
			.andExpect(content().string(not(containsString("contents"))))
			.andExpect(content().string(not(containsString("かざる"))));
	}

	@Test
	void 주인에게는_꾸미기_설정_버튼() throws Exception {
		mvc.perform(get("/post/abcd1234").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("かざる")))
			.andExpect(content().string(containsString("/post/abcd1234/edit")));
	}

	@Test
	void 없는_포스트는_404() throws Exception {
		mvc.perform(get("/post/nothing0"))
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("ポストが見つかりません")));
	}

	@Test
	void 로그인이_필요한_화면은_로그인으로() throws Exception {
		mvc.perform(get("/post/new")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
		mvc.perform(get("/mypage")).andExpect(status().is3xxRedirection());
		mvc.perform(get("/post/abcd1234/decorate")).andExpect(status().is3xxRedirection());
	}

	@Test
	void 로그인_회원_화면() throws Exception {
		given(postService.getMyPosts(1L)).willReturn(List.of(
				new MyPostDto("abcd1234", "ゆうすけのポスト", Theme.SHOGATSU, LocalDateTime.of(2027, 1, 1, 0, 0), 6, false)));
		mvc.perform(get("/mypage").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ゆうすけのポスト")))
			.andExpect(content().string(containsString("受付中")))
			.andExpect(content().string(containsString("11月20日")));
		mvc.perform(get("/mypage/edit").with(user(ownerUser))).andExpect(status().isOk());
		mvc.perform(get("/post/new").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ゆうすけのポスト")))
			.andExpect(content().string(containsString("2027-01-01")));
		mvc.perform(get("/post/abcd1234/created").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("/post/abcd1234")));
		mvc.perform(get("/post/abcd1234/decorate").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("KAGAMI")));
		mvc.perform(get("/post/abcd1234/edit").with(user(ownerUser)))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("ポストを削除")));
	}

	@Test
	void 남의_포스트_꾸미기는_403() throws Exception {
		mvc.perform(get("/post/abcd1234/decorate").with(user(otherUser)))
			.andExpect(status().isForbidden())
			.andExpect(content().string(containsString("アクセス権がありません")));
	}

	@Test
	void 꾸미기_API() throws Exception {
		String body = "{\"decos\":[\"KAGAMI\"],\"nameTag\":\"ゆうすけ\"}";
		// 비로그인 → 401
		mvc.perform(put("/api/posts/abcd1234/decorate").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isUnauthorized());
		// 다른 테마 장식 → INVALID_DECO JSON
		given(postService.decorate(eq("abcd1234"), anyLong(), any())).willThrow(new BusinessException(ErrorCode.INVALID_DECO));
		mvc.perform(put("/api/posts/abcd1234/decorate").with(csrf()).with(user(ownerUser))
				.contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("INVALID_DECO"));
		// 이름표 9자 → 검증 실패
		mvc.perform(put("/api/posts/abcd1234/decorate").with(csrf()).with(user(ownerUser))
				.contentType(MediaType.APPLICATION_JSON).content("{\"decos\":[],\"nameTag\":\"123456789\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("名札は8文字までです"));
	}

	@Test
	void 아이디_중복_확인_API() throws Exception {
		given(memberService.isUseridAvailable(anyString())).willReturn(true);
		mvc.perform(get("/api/members/check-id").param("userid", "newuser1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.available").value(true));
		mvc.perform(get("/api/members/check-id").param("userid", "1bad"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void 메일_전송_API는_코드를_응답에_넣지_않는다() throws Exception {
		mvc.perform(post("/api/mail/send").with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@example.com\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field f = target.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(target, value);
	}
}
