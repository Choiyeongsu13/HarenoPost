package com.harenopost.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.harenopost.dto.PostForm;
import com.harenopost.entity.Deco;
import com.harenopost.entity.Post;
import com.harenopost.entity.Theme;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;

class PostTest {

	private Post post(Theme theme, LocalDateTime openDate) {
		return Post.create("abcd1234", 1L, "テスト", theme, openDate, "なまえ");
	}

	@Test
	void 만들면_테마_장식_3개가_기본값() {
		Post p = post(Theme.SHOGATSU, LocalDateTime.now().plusDays(10));
		assertEquals(List.of("KAGAMI", "SHIMEKAZARI", "KADOMATSU"), p.getDecoCodes());
		assertEquals("KAGAMI,SHIMEKAZARI,KADOMATSU", p.getDecos());
	}

	@Test
	void 꾸미기는_슬롯_순으로_저장() {
		Post p = post(Theme.SHOGATSU, LocalDateTime.now().plusDays(10));
		p.decorate(List.of(Deco.KADOMATSU, Deco.KAGAMI), "ゆうすけ");
		assertEquals("KAGAMI,KADOMATSU", p.getDecos());
		assertEquals("ゆうすけ", p.getNameTag());
	}

	@Test
	void 장식을_모두_빼면_null() {
		Post p = post(Theme.BIRTHDAY, LocalDateTime.now().plusDays(10));
		p.decorate(List.of(), null);
		assertNull(p.getDecos());
		assertTrue(p.getDecoList().isEmpty());
	}

	@Test
	void 다른_테마_장식이나_중복은_INVALID_DECO() {
		Post p = post(Theme.SHOGATSU, LocalDateTime.now().plusDays(10));
		BusinessException e1 = assertThrows(BusinessException.class, () -> p.decorate(List.of(Deco.SANTA), null));
		assertEquals(ErrorCode.INVALID_DECO, e1.getErrorCode());
		BusinessException e2 = assertThrows(BusinessException.class, () -> p.decorate(List.of(Deco.KAGAMI, Deco.KAGAMI), null));
		assertEquals(ErrorCode.INVALID_DECO, e2.getErrorCode());
		List<Deco> withNull = new ArrayList<>();
		withNull.add(null); // 없는 코드
		assertThrows(BusinessException.class, () -> p.decorate(withNull, null));
	}

	@Test
	void 개봉_후에는_꾸미기_설정_불가() {
		Post p = post(Theme.CHRISTMAS, LocalDateTime.now().minusMinutes(1));
		assertTrue(p.isOpened());
		BusinessException e = assertThrows(BusinessException.class, () -> p.decorate(List.of(Deco.SANTA), null));
		assertEquals(ErrorCode.ALREADY_OPENED, e.getErrorCode());
		assertThrows(BusinessException.class, () -> p.changeSettings("x", Theme.CHRISTMAS, LocalDateTime.now().plusDays(1)));
	}

	@Test
	void 기념일을_바꾸면_장식_초기화() {
		Post p = post(Theme.SHOGATSU, LocalDateTime.now().plusDays(10));
		p.decorate(List.of(Deco.KAGAMI), null);
		p.changeSettings("新しい名前", Theme.CHRISTMAS, LocalDateTime.now().plusDays(20));
		assertEquals(List.of("SANTA", "WREATH", "PRESENT"), p.getDecoCodes());

		// 같은 기념일이면 장식 유지
		p.decorate(List.of(Deco.WREATH), null);
		p.changeSettings("名前", Theme.CHRISTMAS, LocalDateTime.now().plusDays(20));
		assertEquals(List.of("WREATH"), p.getDecoCodes());
	}

	@Test
	void 주인_검사() {
		Post p = post(Theme.SHOGATSU, LocalDateTime.now().plusDays(10));
		assertTrue(p.isOwner(1L));
		assertTrue(!p.isOwner(2L));
		assertTrue(!p.isOwner(null));
	}

	// ---------- PostService.resolveOpenDate ----------

	private final PostService service = new PostService(null, null);
	private final LocalDateTime now = LocalDateTime.of(2026, 10, 12, 15, 0);

	private PostForm form(Theme theme, LocalDate date, LocalTime time) {
		PostForm f = new PostForm();
		f.setTheme(theme);
		f.setOpenDate(date);
		f.setOpenTime(time);
		f.setTitle("t");
		return f;
	}

	@Test
	void 고정_테마는_입력값을_무시() {
		assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
				service.resolveOpenDate(form(Theme.SHOGATSU, LocalDate.of(2026, 10, 20), LocalTime.NOON), now));
		assertEquals(LocalDateTime.of(2026, 12, 25, 0, 0),
				service.resolveOpenDate(form(Theme.CHRISTMAS, null, null), now));
	}

	@Test
	void 생일_개봉일_검사() {
		assertEquals(LocalDateTime.of(2026, 11, 20, 18, 30),
				service.resolveOpenDate(form(Theme.BIRTHDAY, LocalDate.of(2026, 11, 20), LocalTime.of(18, 30)), now));
		assertEquals(ErrorCode.OPEN_DATE_REQUIRED, assertThrows(BusinessException.class,
				() -> service.resolveOpenDate(form(Theme.BIRTHDAY, null, null), now)).getErrorCode());
		assertEquals(ErrorCode.OPEN_DATE_PAST, assertThrows(BusinessException.class,
				() -> service.resolveOpenDate(form(Theme.BIRTHDAY, LocalDate.of(2026, 10, 12), LocalTime.of(15, 0)), now)).getErrorCode());
		assertEquals(ErrorCode.OPEN_DATE_TOO_FAR, assertThrows(BusinessException.class,
				() -> service.resolveOpenDate(form(Theme.BIRTHDAY, LocalDate.of(2027, 10, 13), null), now)).getErrorCode());
	}
}
