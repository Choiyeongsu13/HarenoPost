package com.harenopost.report;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByCardIdxAndStatus(Long cardIdx, String status);
}
