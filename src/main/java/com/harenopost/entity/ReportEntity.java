package com.harenopost.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//카드 신고
@Entity
@Table(name = "tbl_report")
@NoArgsConstructor
@Getter
public class ReportEntity {

	public static final String WAIT = "WAIT";
	public static final String DONE = "DONE";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_report_GENERATOR")
	@SequenceGenerator(name = "seq_report_GENERATOR", sequenceName = "seq_report", allocationSize = 1)
	@Column(name = "report_idx")
	private Long reportIdx;

	@Column(name = "card_idx")
	private Long cardIdx;

	private String reason;

	private String status = WAIT; //WAIT / DONE

	private LocalDateTime regdate = LocalDateTime.now();

	@Builder
	public ReportEntity(Long cardIdx, String reason) {
		this.cardIdx = cardIdx;
		this.reason = reason;
	}

	//처리 완료
	public void reportDone() {
		this.status = DONE;
	}
}
