package com.harenopost.dto;

import java.time.LocalDateTime;

import com.harenopost.entity.DesignEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

//카드 디자인 (카드 모달, 관리자 목록)
@NoArgsConstructor
@Getter
public class DesignResponseDTO {

	private Long designIdx;
	private String name;
	private Theme theme; //null = 공통
	private String originName;
	private String imageUrl;
	private Integer sortOrder;
	private String useYn;
	private LocalDateTime regdate;

	//entity -> dto
	public DesignResponseDTO(DesignEntity entity) {
		this.designIdx = entity.getDesignIdx();
		this.name = entity.getName();
		this.theme = entity.getTheme();
		this.originName = entity.getOriginName();
		this.imageUrl = entity.getImageUrl();
		this.sortOrder = entity.getSortOrder();
		this.useYn = entity.getUseYn();
		this.regdate = entity.getRegdate();
	}

	//화면 표시용 테마 이름
	public String getThemeLabel() {
		return theme == null ? "共通" : theme.getLabel();
	}
}
