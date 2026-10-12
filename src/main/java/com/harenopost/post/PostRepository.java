package com.harenopost.post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	Optional<Post> findByCode(String code);

	boolean existsByCode(String code);

	List<Post> findByMemberIdxOrderByRegdateDesc(Long memberIdx);

	/** 受付中(개봉 전) 포스트 수 */
	long countByMemberIdxAndOpenDateAfter(Long memberIdx, LocalDateTime now);
}
