package com.harenopost.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.ReportEntity;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
	//save(entity) //신고 등록, 처리 완료

	//1. 같은 카드에 처리 대기 중인 신고가 있는지 (중복 신고 방지)
	boolean existsByCardIdxAndStatus(Long cardIdx, String status);

	//2. 상태별 신고 목록 (최신순)
	List<ReportEntity> findByStatusOrderByRegdateDesc(String status);
}
