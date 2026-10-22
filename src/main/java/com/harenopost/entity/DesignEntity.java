package com.harenopost.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.harenopost.dto.Theme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//카드 디자인 (관리자가 업로드)
@Entity
@Table(name = "tbl_design")
@NoArgsConstructor
@Getter
public class DesignEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_design_GENERATOR")
	@SequenceGenerator(name = "seq_design_GENERATOR", sequenceName = "seq_design", allocationSize = 1)
	@Column(name = "design_idx")
	private Long designIdx;

	private String name;

	@Enumerated(EnumType.STRING)
	private Theme theme; //null = 공통

	@Column(name = "origin_name")
	private String originName; //원래 파일명

	@Column(name = "save_name")
	private String saveName; //UUID 파일명

	@Column(name = "sort_order")
	private Integer sortOrder = 0;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "use_yn")
	private String useYn = "Y";

	private LocalDateTime regdate = LocalDateTime.now();

	@Builder
	public DesignEntity(String name, Theme theme, String originName, String saveName) {
		this.name = name;
		this.theme = theme;
		this.originName = originName;
		this.saveName = saveName;
	}

	//사용 여부·표시 순서 수정
	public void designModify(String useYn, Integer sortOrder) {
		this.useYn = useYn;
		this.sortOrder = sortOrder;
	}

	//화면에 보여 줄 이미지 주소
	public String getImageUrl() {
		return saveName == null ? null : "/upload/" + saveName;
	}
}
