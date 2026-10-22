package com.harenopost.dto;

import com.harenopost.entity.CardEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//카드 보내기 입력값 (ajax). 입력 규칙은 서비스에서 검사한다
@NoArgsConstructor
@Setter
@Getter
public class CardRequestDTO {

	private String code; //포스트 공유 코드
	private Long designIdx; //null 이면 기본 카드 모양
	private String senderName; //보낸 이름 1〜10자
	private String contents; //본문 1〜300자

	//DTO에서 필요한 부분을 entity화 (포스트 번호, IP 는 서비스에서 정한다)
	public CardEntity toEntity(Long postIdx, String ip) {
		return CardEntity.builder()
				.postIdx(postIdx)
				.designIdx(designIdx)
				.senderName(senderName.strip())
				.contents(contents.strip())
				.ip(ip)
				.build();
	}
}
