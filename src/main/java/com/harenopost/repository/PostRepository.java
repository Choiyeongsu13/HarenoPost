package com.harenopost.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.PostEntity;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
	//save(entity) //등록, 수정
	//delete(entity) //삭제 (카드·신고는 DB CASCADE)

	//1. 공유 코드로 검색
	Optional<PostEntity> findByCode(String code);

	//2. 코드 중복검사
	boolean existsByCode(String code);

	//3. 내 포스트 목록 (최신순)
	List<PostEntity> findByUserIdxOrderByRegdateDesc(Long userIdx);

	//4. 受付中(개봉 전) 포스트 수 (5개 제한)
	long countByUserIdxAndOpenDateAfter(Long userIdx, LocalDateTime now);
}
