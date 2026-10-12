package com.harenopost.card;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.card.dto.EnvelopeDto;
import com.harenopost.design.Design;
import com.harenopost.design.DesignRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

	private final CardRepository cardRepository;
	private final DesignRepository designRepository;

	public long countByPost(Long postIdx) {
		return cardRepository.countByPostIdx(postIdx);
	}

	/** postIdx → 카드 수 */
	public Map<Long, Long> countByPosts(Collection<Long> postIdxs) {
		Map<Long, Long> map = new HashMap<>();
		if (postIdxs.isEmpty()) {
			return map;
		}
		for (Object[] row : cardRepository.countGroupByPostIdx(postIdxs)) {
			map.put((Long) row[0], (Long) row[1]);
		}
		return map;
	}

	/** 봉투 목록 (보낸 이름 + 디자인 이미지만, 본문 없음) */
	public List<EnvelopeDto> getEnvelopes(Long postIdx) {
		List<Card> cards = cardRepository.findByPostIdxOrderByRegdateAsc(postIdx);
		List<Long> designIdxs = cards.stream().map(Card::getDesignIdx).filter(Objects::nonNull).distinct().toList();
		Map<Long, Design> designs = designRepository.findAllById(designIdxs).stream()
				.collect(Collectors.toMap(Design::getDesignIdx, Function.identity()));
		return cards.stream().map(c -> {
			Design d = c.getDesignIdx() == null ? null : designs.get(c.getDesignIdx());
			return new EnvelopeDto(c.getCardIdx(), c.getSenderName(), d == null ? null : d.getImageUrl(), c.isReadYn());
		}).toList();
	}
}
