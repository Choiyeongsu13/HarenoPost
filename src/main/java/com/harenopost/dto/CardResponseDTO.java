package com.harenopost.dto;

import java.time.LocalDateTime;

import com.harenopost.entity.CardEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

//카드 한 장 (본문 포함).
//주인 + 개봉 후가 확인된 카드 목록 화면에서만 쓴다. 포스트 화면에는 EnvelopeResponseDTO 를 쓴다.
@NoArgsConstructor
@Getter
public class CardResponseDTO {

	private Long cardIdx;
	private String senderName;
	private String contents;
	private String imageUrl; //카드 디자인 이미지 (없으면 null)
	private boolean read;
	private LocalDateTime regdate;

	//entity -> dto
	public CardResponseDTO(CardEntity entity, String imageUrl) {
		this.cardIdx = entity.getCardIdx();
		this.senderName = entity.getSenderName();
		this.contents = entity.getContents();
		this.imageUrl = imageUrl;
		this.read = entity.isReadYn();
		this.regdate = entity.getRegdate();
	}
}
