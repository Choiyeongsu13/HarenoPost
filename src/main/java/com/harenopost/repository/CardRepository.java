package com.harenopost.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.harenopost.entity.CardEntity;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

	//1. 포스트의 카드 수
	long countByPostIdx(Long postIdx);

	//2. 포스트의 카드 목록 (보낸 순)
	List<CardEntity> findByPostIdxOrderByRegdateAsc(Long postIdx);

	//3. 도배 검사: 이 IP 가 기준 시각 이후에 보낸 카드가 있는지
	boolean existsByIpAndRegdateAfter(String ip, LocalDateTime time);

	//4. 포스트별 카드 수 [postIdx, count] (마이페이지 목록용)
	@Query("select card.postIdx, count(card) from CardEntity card where card.postIdx in :postIdxs group by card.postIdx")
	List<Object[]> cardCountList(@Param("postIdxs") Collection<Long> postIdxs);
}
