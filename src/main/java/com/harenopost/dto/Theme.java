package com.harenopost.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 기념일(테마). 기념일을 늘릴 때는 여기와 Deco, theme-art.js 에 추가한다 */
@Getter
@RequiredArgsConstructor
public enum Theme {

	SHOGATSU("お正月", MonthDay.of(1, 1)),
	BIRTHDAY("誕生日", null),
	CHRISTMAS("クリスマス", MonthDay.of(12, 25));

	private final String label;
	/** 개봉일이 고정인 테마의 월일. 誕生日 은 null */
	private final MonthDay fixedDay;

	public boolean isFixed() {
		return fixedDay != null;
	}

	/** 화면용 소문자 키 (theme-art.js 의 THEMES 키, CSS theme-xxx) */
	public String getKey() {
		return name().toLowerCase();
	}

	/**
	 * 다음 개봉일 기본값 (now 이후 가장 가까운 날짜의 0:00).
	 * 誕生日 은 생일이 없으면 null.
	 */
	public LocalDateTime nextOpenDate(LocalDateTime now, LocalDate birthday) {
		MonthDay day = isFixed() ? fixedDay : (birthday == null ? null : MonthDay.from(birthday));
		if (day == null) {
			return null;
		}
		// 2/29 생일은 평년에는 2/28 로 맞춰진다 (MonthDay.atYear)
		LocalDateTime candidate = day.atYear(now.getYear()).atStartOfDay();
		if (!candidate.isAfter(now)) {
			candidate = day.atYear(now.getYear() + 1).atStartOfDay();
		}
		return candidate;
	}
}
