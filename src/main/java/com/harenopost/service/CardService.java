package com.harenopost.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.CardRequestDTO;
import com.harenopost.dto.CardResponseDTO;
import com.harenopost.dto.EnvelopeResponseDTO;
import com.harenopost.entity.CardEntity;
import com.harenopost.entity.DesignEntity;
import com.harenopost.entity.PostEntity;
import com.harenopost.entity.ReportEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.CardRepository;
import com.harenopost.repository.DesignRepository;
import com.harenopost.repository.PostRepository;
import com.harenopost.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

	private static final int SENDER_NAME_MAX = 10;
	private static final int CONTENTS_MAX = 300;
	private static final int REASON_MAX = 100;

	private final CardRepository cardRepository;
	private final DesignRepository designRepository;
	private final PostRepository postRepository;
	private final ReportRepository reportRepository;

	//포스트의 카드 수
	@Transactional(readOnly = true)
	public long cardCount(Long postIdx) {
		return cardRepository.countByPostIdx(postIdx);
	}

	//포스트별 카드 수 (postIdx → 카드 수)
	@Transactional(readOnly = true)
	public Map<Long, Long> cardCountList(Collection<Long> postIdxs) {
		Map<Long, Long> map = new HashMap<>();
		if (postIdxs.isEmpty()) {
			return map;
		}
		for (Object[] row : cardRepository.cardCountList(postIdxs)) {
			map.put((Long) row[0], (Long) row[1]);
		}
		return map;
	}

	//봉투 목록 (보낸 이름 + 디자인 이미지만, 본문 없음)
	@Transactional(readOnly = true)
	public List<EnvelopeResponseDTO> envelopeList(Long postIdx) {
		List<CardEntity> cards = cardRepository.findByPostIdxOrderByRegdateAsc(postIdx);
		Map<Long, String> images = imageMap(cards);
		return cards.stream()
				.map(card -> new EnvelopeResponseDTO(card.getCardIdx(), card.getSenderName(),
						images.get(card.getDesignIdx()), card.isReadYn()))
				.toList();
	}

	//카드 보내기 (누구나, 개봉 전)
	//검사 순서: 포스트 존재 → 개봉 전 → 입력 규칙 → 디자인 → 같은 IP 1분 1장
	@Transactional
	public void cardWrite(CardRequestDTO cardRequestDTO, String ip) {
		PostEntity postEntity = postRepository.findByCode(cardRequestDTO.getCode())
				.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		if (postEntity.isOpened()) {
			throw new BusinessException(ErrorCode.RECEIVE_CLOSED);
		}
		if (!lengthOk(cardRequestDTO.getSenderName(), SENDER_NAME_MAX)) {
			throw new BusinessException(ErrorCode.SENDER_NAME_INVALID);
		}
		if (!lengthOk(cardRequestDTO.getContents(), CONTENTS_MAX)) {
			throw new BusinessException(ErrorCode.CONTENTS_INVALID);
		}
		if (cardRequestDTO.getDesignIdx() != null) {
			DesignEntity design = designRepository.findById(cardRequestDTO.getDesignIdx())
					.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_DESIGN));
			boolean themeOk = design.getTheme() == null || design.getTheme() == postEntity.getTheme();
			if (!"Y".equals(design.getUseYn()) || !themeOk) {
				throw new BusinessException(ErrorCode.INVALID_DESIGN);
			}
		}
		//도배 방지: 같은 IP 의 마지막 카드가 1분 이내면 거절
		if (cardRepository.existsByIpAndRegdateAfter(ip, LocalDateTime.now().minusMinutes(1))) {
			throw new BusinessException(ErrorCode.CARD_TOO_FAST);
		}
		cardRepository.save(cardRequestDTO.toEntity(postEntity.getPostIdx(), ip));
	}

	//카드 목록 (주인, 개봉 후). 본문 포함
	@Transactional(readOnly = true)
	public List<CardResponseDTO> cardList(String code, Long loginIdx) {
		PostEntity postEntity = postRepository.findByCode(code)
				.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		postOwnerOpenedCheck(postEntity, loginIdx);
		List<CardEntity> cards = cardRepository.findByPostIdxOrderByRegdateAsc(postEntity.getPostIdx());
		Map<Long, String> images = imageMap(cards);
		return cards.stream().map(card -> new CardResponseDTO(card, images.get(card.getDesignIdx()))).toList();
	}

	//읽음 처리 (주인, 개봉 후)
	@Transactional
	public void cardRead(Long cardIdx, Long loginIdx) {
		cardOwnerFind(cardIdx, loginIdx).cardRead();
	}

	//카드 신고 (주인, 개봉 후). 처리 대기 중인 신고가 있으면 거절
	@Transactional
	public void cardReport(Long cardIdx, Long loginIdx, String reason) {
		CardEntity cardEntity = cardOwnerFind(cardIdx, loginIdx);
		if (!lengthOk(reason, REASON_MAX)) {
			throw new BusinessException(ErrorCode.REPORT_REASON_INVALID);
		}
		if (reportRepository.existsByCardIdxAndStatus(cardEntity.getCardIdx(), ReportEntity.WAIT)) {
			throw new BusinessException(ErrorCode.ALREADY_REPORTED);
		}
		reportRepository.save(ReportEntity.builder().cardIdx(cardEntity.getCardIdx()).reason(reason.strip()).build());
	}

	//카드 삭제 (관리자). 신고는 DB 의 ON DELETE CASCADE 로 함께 지워진다
	@Transactional
	public void cardDelete(Long cardIdx) {
		CardEntity cardEntity = cardRepository.findById(cardIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
		cardRepository.delete(cardEntity);
	}

	//카드 → 포스트 → 주인·개봉 후 확인
	private CardEntity cardOwnerFind(Long cardIdx, Long loginIdx) {
		CardEntity cardEntity = cardRepository.findById(cardIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
		PostEntity postEntity = postRepository.findById(cardEntity.getPostIdx())
				.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		postOwnerOpenedCheck(postEntity, loginIdx);
		return cardEntity;
	}

	//주인이 아니면 403, 개봉 전이면 NOT_OPENED_YET
	private void postOwnerOpenedCheck(PostEntity postEntity, Long loginIdx) {
		if (!postEntity.isOwner(loginIdx)) {
			throw new BusinessException(ErrorCode.NOT_OWNER);
		}
		if (!postEntity.isOpened()) {
			throw new BusinessException(ErrorCode.NOT_OPENED_YET);
		}
	}

	//카드들이 쓰는 디자인 이미지 (designIdx → imageUrl)
	private Map<Long, String> imageMap(List<CardEntity> cards) {
		List<Long> designIdxs = cards.stream().map(CardEntity::getDesignIdx).filter(Objects::nonNull).distinct().toList();
		return designRepository.findAllById(designIdxs).stream()
				.filter(d -> d.getImageUrl() != null)
				.collect(Collectors.toMap(DesignEntity::getDesignIdx, DesignEntity::getImageUrl, (a, b) -> a));
	}

	//앞뒤 공백을 뺀 글자 수가 1〜max 인지 (이모지도 한 글자)
	private static boolean lengthOk(String s, int max) {
		if (s == null || s.isBlank()) {
			return false;
		}
		String t = s.strip();
		return t.codePointCount(0, t.length()) <= max;
	}
}
