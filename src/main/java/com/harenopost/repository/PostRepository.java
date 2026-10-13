package com.harenopost.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Optional<Post> findByCode(String code);

	boolean existsByCode(String code);

	List<Post> findByUserIdxOrderByRegdateDesc(Long userIdx);

	/** 受付中(개봉 전) 포스트 수 */
	long countByUserIdxAndOpenDateAfter(Long userIdx, LocalDateTime now);
}
