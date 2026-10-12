package com.harenopost.card.dto;

/**
 * 포스트 화면의 봉투 하나.
 * 개봉 전 본문 비노출 규칙 때문에 contents 필드를 두지 않는다.
 */
public record EnvelopeDto(Long cardIdx, String senderName, String imageUrl, boolean isRead) {
}
