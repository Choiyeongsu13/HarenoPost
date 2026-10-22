package com.harenopost.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.harenopost.dto.Theme;
import com.harenopost.entity.DesignEntity;

public interface DesignRepository extends JpaRepository<DesignEntity, Long> {
	//findAllById(ids) //봉투 이미지
	//save(entity) //등록, 수정
	//delete(entity) //삭제 (이 디자인을 쓴 카드는 DB 에서 design_idx = NULL)

	//1. 카드 모달용: 사용 중(Y)이고 이 테마 전용이거나 공통(theme IS NULL)인 디자인 (순서대로)
	@Query("select design from DesignEntity design "
			+ " where design.useYn = 'Y' and (design.theme = :theme or design.theme is null) "
			+ " order by design.sortOrder, design.designIdx")
	List<DesignEntity> designList(@Param("theme") Theme theme);

	//2. 관리자 목록 (순서대로)
	List<DesignEntity> findAllByOrderBySortOrderAscDesignIdxAsc();
}
