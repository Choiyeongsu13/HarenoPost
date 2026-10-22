package com.harenopost.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//포스트 화면의 봉투 하나.
//개봉 전 본문 비노출 규칙 때문에 contents 필드를 두지 않는다.
@NoArgsConstructor
@Getter
public class EnvelopeResponseDTO {

	private Long cardIdx;
	private String senderName;
	private String imageUrl; //카드 디자인 이미지 (없으면 null)
	private boolean read;

	public EnvelopeResponseDTO(Long cardIdx, String senderName, String imageUrl, boolean read) {
		this.cardIdx = cardIdx;
		this.senderName = senderName;
		this.imageUrl = imageUrl;
		this.read = read;
	}
}
