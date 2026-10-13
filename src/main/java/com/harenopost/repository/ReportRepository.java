package com.harenopost.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

	boolean existsByCardIdxAndStatus(Long cardIdx, String status);
}
