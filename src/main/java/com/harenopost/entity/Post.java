package com.harenopost.entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;

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

@Entity
@Table(name = "tbl_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

	@Id
	@SequenceGenerator(name = "seq_post_gen", sequenceName = "seq_post", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_post_gen")
	@Column(name = "post_idx")
	private Long postIdx;

	@Column(nullable = false, unique = true, length = 20)
	private String code;

	@Column(name = "user_idx", nullable = false)
	private Long userIdx; // @ManyToOne 대신 FK 값

	@Column(nullable = false, length = 60)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Theme theme;

	@Column(name = "open_date", nullable = false)
	private LocalDateTime openDate;

	@Column(length = 100)
	private String decos; // 장식 코드 CSV (슬롯 순). 없으면 null

	@Column(name = "name_tag", length = 30)
	private String nameTag;

	@Column(insertable = false, updatable = false)
	private LocalDateTime regdate; // DB DEFAULT

	public static Post create(String code, Long userIdx, String title, Theme theme, LocalDateTime openDate, String nameTag) {
		Post p = new Post();
		p.code = code;
		p.userIdx = userIdx;
		p.title = title;
		p.theme = theme;
		p.openDate = openDate;
		p.decos = toCsv(Deco.of(theme)); // 기본값: 테마 장식 3개 전부
		p.nameTag = nameTag;
		return p;
	}

	public boolean isOpened() {
		return !LocalDateTime.now().isBefore(openDate);
	}

	public boolean isOwner(Long loginIdx) {
		return loginIdx != null && userIdx.equals(loginIdx);
	}

	/** CSV → Deco 목록 (모르는 코드는 건너뜀) */
	public List<Deco> getDecoList() {
		if (decos == null || decos.isBlank()) {
			return List.of();
		}
		return Arrays.stream(decos.split(",")).map(Deco::fromCode).filter(Objects::nonNull).toList();
	}

	/** 화면 JS 용 코드 목록 ["KAGAMI", ...] */
	public List<String> getDecoCodes() {
		return getDecoList().stream().map(Deco::name).toList();
	}

	/** 꾸미기: 이 테마의 장식만, 중복 없이. 슬롯 순 CSV 로 저장 */
	public void decorate(List<Deco> newDecos, String newNameTag) {
		if (isOpened()) {
			throw new BusinessException(ErrorCode.ALREADY_OPENED);
		}
		if (newDecos.stream().anyMatch(d -> d == null || d.getTheme() != theme)
				|| new HashSet<>(newDecos).size() != newDecos.size()) {
			throw new BusinessException(ErrorCode.INVALID_DECO);
		}
		this.decos = toCsv(newDecos);
		this.nameTag = newNameTag;
	}

	/** 설정 변경. 기념일이 바뀌면 장식을 새 테마 기본값으로 초기화 */
	public void changeSettings(String title, Theme theme, LocalDateTime openDate) {
		if (isOpened()) {
			throw new BusinessException(ErrorCode.ALREADY_OPENED);
		}
		if (this.theme != theme) {
			this.decos = toCsv(Deco.of(theme));
		}
		this.title = title;
		this.theme = theme;
		this.openDate = openDate;
	}

	private static String toCsv(List<Deco> list) {
		if (list.isEmpty()) {
			return null;
		}
		return list.stream().sorted(Comparator.comparing(Deco::getSlot)).map(Deco::name).collect(Collectors.joining(","));
	}
}
