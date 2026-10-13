package com.harenopost.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 카드 신고 */
@Entity
@Table(name = "tbl_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

	public static final String WAIT = "WAIT";
	public static final String DONE = "DONE";

	@Id
	@SequenceGenerator(name = "seq_report_gen", sequenceName = "seq_report", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_report_gen")
	@Column(name = "report_idx")
	private Long reportIdx;

	@Column(name = "card_idx", nullable = false)
	private Long cardIdx;

	@Column(nullable = false, length = 500)
	private String reason;

	@Column(nullable = false, length = 10)
	private String status;

	@Column(insertable = false, updatable = false)
	private LocalDateTime regdate;

	public static Report create(Long cardIdx, String reason) {
		Report r = new Report();
		r.cardIdx = cardIdx;
		r.reason = reason;
		r.status = WAIT;
		return r;
	}

	public void done() {
		this.status = DONE;
	}
}
