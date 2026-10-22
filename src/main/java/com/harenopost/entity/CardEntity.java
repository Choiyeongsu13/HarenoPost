package com.harenopost.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

@Entity
@Table(name = "tbl_card")
@NoArgsConstructor
@Getter
public class CardEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_card_GENERATOR")
	@SequenceGenerator(name = "seq_card_GENERATOR", sequenceName = "seq_card", allocationSize = 1)
	@Column(name = "card_idx")
	private Long cardIdx;

	@Column(name = "post_idx")
	private Long postIdx;

	@Column(name = "design_idx")
	private Long designIdx; //null 이면 기본 카드 모양

	@Column(name = "sender_name")
	private String senderName;

	private String contents;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "is_read")
	private String isRead = "N"; //Y / N

	private String ip; //도배 검사

	private LocalDateTime regdate = LocalDateTime.now();

	@Builder
	public CardEntity(Long postIdx, Long designIdx, String senderName, String contents, String ip) {
		this.postIdx = postIdx;
		this.designIdx = designIdx;
		this.senderName = senderName;
		this.contents = contents;
		this.ip = ip;
	}

	public boolean isReadYn() {
		return "Y".equals(isRead);
	}

	//읽음 처리
	public void cardRead() {
		this.isRead = "Y";
	}
}
