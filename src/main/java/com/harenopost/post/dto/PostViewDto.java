package com.harenopost.post.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.harenopost.post.Post;
import com.harenopost.post.Theme;

/** 포스트 화면 모델. 카드 본문은 들어가지 않는다 */
public record PostViewDto(String code, String title, Theme theme, LocalDateTime openDate, String nameTag,
		List<String> decoCodes, boolean opened, boolean owner) {

	private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
	private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

	public static PostViewDto of(Post post, Long loginIdx) {
		return new PostViewDto(post.getCode(), post.getTitle(), post.getTheme(), post.getOpenDate(), post.getNameTag(),
				post.getDecoCodes(), post.isOpened(), post.isOwner(loginIdx));
	}

	/** 카운트다운용 ISO 문자열 (+09:00 포함) */
	public String getOpenDateIso() {
		return openDate.atZone(JST).format(ISO);
	}
}
