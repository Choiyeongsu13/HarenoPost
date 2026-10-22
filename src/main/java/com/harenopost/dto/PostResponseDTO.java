package com.harenopost.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.harenopost.entity.PostEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

//포스트 정보 (포스트 화면, 꾸미기, 설정, 마이페이지 목록). 카드 본문은 들어가지 않는다
@NoArgsConstructor
@Getter
public class PostResponseDTO {

	private static final ZoneId JST = ZoneId.of("Asia/Tokyo");
	private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

	private Long postIdx;
	private String code;
	private Long userIdx;
	private String title;
	private Theme theme;
	private LocalDateTime openDate;
	private String nameTag;
	private List<String> decoCodes; //["KAGAMI", ...]
	private boolean opened; //개봉일이 지났는지
	private boolean owner; //로그인 회원이 주인인지
	private long cardCount; //받은 카드 수 (마이페이지)

	//entity -> dto
	public PostResponseDTO(PostEntity entity) {
		this.postIdx = entity.getPostIdx();
		this.code = entity.getCode();
		this.userIdx = entity.getUserIdx();
		this.title = entity.getTitle();
		this.theme = entity.getTheme();
		this.openDate = entity.getOpenDate();
		this.nameTag = entity.getNameTag();
		this.decoCodes = entity.getDecoCodes();
		this.opened = entity.isOpened();
	}

	//entity -> dto (주인 여부, 카드 수 포함)
	public PostResponseDTO(PostEntity entity, Long loginIdx, long cardCount) {
		this(entity);
		this.owner = entity.isOwner(loginIdx);
		this.cardCount = cardCount;
	}

	//카운트다운용 ISO 문자열 (+09:00 포함)
	public String getOpenDateIso() {
		return openDate.atZone(JST).format(ISO);
	}

	//마이페이지 목록 아이콘 색
	public String getMiniColor() {
		switch (theme) {
		case SHOGATSU:
			return "#E9473F";
		case BIRTHDAY:
			return "#E0567A";
		default:
			return "#1E4A3A";
		}
	}

	//마이페이지 목록 링크: 개봉 전 → 포스트 화면, 개봉 후 → 카드 목록
	public String getLink() {
		return opened ? "/Card/card_list?code=" + code : "/post/" + code;
	}
}
