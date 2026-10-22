package com.harenopost.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ThemeTest {

	@Test
	void 오쇼가츠는_다음_1월1일() {
		assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
				Theme.SHOGATSU.nextOpenDate(LocalDateTime.of(2026, 10, 12, 15, 0), null));
		// 1/1 0:00 정각이면 이미 지난 것으로 보고 다음 해
		assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
				Theme.SHOGATSU.nextOpenDate(LocalDateTime.of(2027, 1, 1, 0, 0), null));
	}

	@Test
	void 크리스마스는_다음_12월25일() {
		assertEquals(LocalDateTime.of(2026, 12, 25, 0, 0),
				Theme.CHRISTMAS.nextOpenDate(LocalDateTime.of(2026, 10, 12, 15, 0), null));
		assertEquals(LocalDateTime.of(2027, 12, 25, 0, 0),
				Theme.CHRISTMAS.nextOpenDate(LocalDateTime.of(2026, 12, 25, 9, 0), null));
	}

	@Test
	void 생일은_다음_생일_0시() {
		LocalDate birthday = LocalDate.of(2000, 11, 20);
		assertEquals(LocalDateTime.of(2026, 11, 20, 0, 0),
				Theme.BIRTHDAY.nextOpenDate(LocalDateTime.of(2026, 10, 12, 15, 0), birthday));
		assertEquals(LocalDateTime.of(2027, 11, 20, 0, 0),
				Theme.BIRTHDAY.nextOpenDate(LocalDateTime.of(2026, 11, 20, 0, 1), birthday));
	}

	@Test
	void 생일이_없으면_null() {
		assertNull(Theme.BIRTHDAY.nextOpenDate(LocalDateTime.now(), null));
	}

	@Test
	void 윤일_생일은_평년에_2월28일() {
		assertEquals(LocalDateTime.of(2027, 2, 28, 0, 0),
				Theme.BIRTHDAY.nextOpenDate(LocalDateTime.of(2026, 10, 12, 0, 0), LocalDate.of(2004, 2, 29)));
	}
}
