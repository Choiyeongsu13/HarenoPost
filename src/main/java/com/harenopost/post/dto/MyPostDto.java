package com.harenopost.post.dto;

import java.time.LocalDateTime;

import com.harenopost.post.Theme;

/** 마이페이지의 내 포스트 한 줄 */
public record MyPostDto(String code, String title, Theme theme, LocalDateTime openDate, long cardCount, boolean opened) {

	/** 목록 아이콘 색 */
	public String getMiniColor() {
		return switch (theme) {
			case SHOGATSU -> "#E9473F";
			case BIRTHDAY -> "#E0567A";
			case CHRISTMAS -> "#1E4A3A";
		};
	}

	/** 개봉 전 → 포스트 화면, 개봉 후 → 카드 목록 */
	public String getLink() {
		return opened ? "/post/" + code + "/cards" : "/post/" + code;
	}
}
