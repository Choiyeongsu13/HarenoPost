package com.harenopost.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 카드 디자인 (관리자가 업로드)
@Entity
@Table(name = "tbl_design")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Design {

	@Id
	@SequenceGenerator(name = "seq_design_gen", sequenceName = "seq_design", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_design_gen")
	@Column(name = "design_idx")
	private Long designIdx;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Theme theme; // null = 공통

	@Column(name = "origin_name", length = 255)
	private String originName;

	@Column(name = "save_name", unique = true, length = 255)
	private String saveName;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "use_yn", nullable = false, length = 1)
	private String useYn;

	@Column(insertable = false, updatable = false)
	private LocalDateTime regdate;

	public String getImageUrl() {
		return saveName == null ? null : "/upload/" + saveName;
	}
}
