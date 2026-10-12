package com.harenopost.card;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {

	long countByPostIdx(Long postIdx);

	List<Card> findByPostIdxOrderByRegdateAsc(Long postIdx);

	/** 도배 검사: 이 IP 가 기준 시각 이후에 보낸 카드가 있는지 */
	boolean existsByIpAndRegdateAfter(String ip, LocalDateTime time);

	/** 포스트별 카드 수 [postIdx, count] (마이페이지 목록용) */
	@Query("select c.postIdx, count(c) from Card c where c.postIdx in :postIdxs group by c.postIdx")
	List<Object[]> countGroupByPostIdx(@Param("postIdxs") Collection<Long> postIdxs);
}
