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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

	@Id
	@SequenceGenerator(name = "seq_card_gen", sequenceName = "seq_card", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_card_gen")
	@Column(name = "card_idx")
	private Long cardIdx;

	@Column(name = "post_idx", nullable = false)
	private Long postIdx;

	@Column(name = "design_idx")
	private Long designIdx; // null 이면 기본 카드 모양

	@Column(name = "sender_name", nullable = false, length = 60)
	private String senderName;

	@Column(nullable = false, length = 1000)
	private String contents;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "is_read", nullable = false, length = 1)
	private String isRead;

	@Column(nullable = false, length = 50)
	private String ip;

	@Column(insertable = false, updatable = false)
	private LocalDateTime regdate;

	public static Card create(Long postIdx, Long designIdx, String senderName, String contents, String ip) {
		Card c = new Card();
		c.postIdx = postIdx;
		c.designIdx = designIdx;
		c.senderName = senderName;
		c.contents = contents;
		c.isRead = "N";
		c.ip = ip;
		return c;
	}

	public boolean isReadYn() {
		return "Y".equals(isRead);
	}

	public void markRead() {
		this.isRead = "Y";
	}
}
