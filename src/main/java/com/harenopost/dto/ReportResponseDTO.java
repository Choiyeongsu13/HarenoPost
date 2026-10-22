package com.harenopost.dto;

import java.time.LocalDateTime;

import com.harenopost.entity.CardEntity;
import com.harenopost.entity.PostEntity;
import com.harenopost.entity.ReportEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

//관리자 신고 목록 한 줄 (신고 + 카드 + 포스트)
@NoArgsConstructor
@Getter
public class ReportResponseDTO {

	private Long reportIdx;
	private String reason;
	private String status;
	private LocalDateTime regdate;
	private Long cardIdx;
	private String senderName;
	private String contents;
	private String postTitle;
	private String postCode;

	//entity -> dto
	public ReportResponseDTO(ReportEntity report, CardEntity card, PostEntity post) {
		this.reportIdx = report.getReportIdx();
		this.reason = report.getReason();
		this.status = report.getStatus();
		this.regdate = report.getRegdate();
		this.cardIdx = report.getCardIdx();
		this.senderName = card == null ? "" : card.getSenderName();
		this.contents = card == null ? "" : card.getContents();
		this.postTitle = post == null ? "" : post.getTitle();
		this.postCode = post == null ? "" : post.getCode();
	}
}
