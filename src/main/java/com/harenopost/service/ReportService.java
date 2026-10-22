package com.harenopost.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.ReportResponseDTO;
import com.harenopost.entity.CardEntity;
import com.harenopost.entity.PostEntity;
import com.harenopost.entity.ReportEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.CardRepository;
import com.harenopost.repository.PostRepository;
import com.harenopost.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final CardRepository cardRepository;
	private final PostRepository postRepository;

	//상태별 신고 목록 (관리자). 카드·포스트는 서비스에서 한 번 더 조회해서 붙인다
	@Transactional(readOnly = true)
	public List<ReportResponseDTO> reportList(String status) {
		List<ReportEntity> reports = reportRepository.findByStatusOrderByRegdateDesc(status);
		Map<Long, CardEntity> cards = cardRepository.findAllById(reports.stream().map(ReportEntity::getCardIdx).distinct().toList())
				.stream().collect(Collectors.toMap(CardEntity::getCardIdx, Function.identity()));
		Map<Long, PostEntity> posts = postRepository.findAllById(cards.values().stream().map(CardEntity::getPostIdx).distinct().toList())
				.stream().collect(Collectors.toMap(PostEntity::getPostIdx, Function.identity()));
		return reports.stream().map(report -> {
			CardEntity card = cards.get(report.getCardIdx());
			PostEntity post = card == null ? null : posts.get(card.getPostIdx());
			return new ReportResponseDTO(report, card, post);
		}).toList();
	}

	//처리 완료
	@Transactional
	public void reportDone(Long reportIdx) {
		reportRepository.findById(reportIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND))
				.reportDone();
	}
}
