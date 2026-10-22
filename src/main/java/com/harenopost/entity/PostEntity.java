package com.harenopost.entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.harenopost.dto.Deco;
import com.harenopost.dto.Theme;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_post")
@NoArgsConstructor
@Getter
public class PostEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_post_GENERATOR")
	@SequenceGenerator(name = "seq_post_GENERATOR", sequenceName = "seq_post", allocationSize = 1)
	@Column(name = "post_idx")
	private Long postIdx;

	private String code; //공유용 8자

	@Column(name = "user_idx")
	private Long userIdx; //주인 (@ManyToOne 대신 FK 값)

	private String title;

	@Enumerated(EnumType.STRING)
	private Theme theme;

	@Column(name = "open_date")
	private LocalDateTime openDate; //개봉일

	private String decos; //장식 코드 CSV (슬롯 순). 없으면 null

	@Column(name = "name_tag")
	private String nameTag; //이름표

	private LocalDateTime regdate = LocalDateTime.now();

	@Builder
	public PostEntity(String code, Long userIdx, String title, Theme theme, LocalDateTime openDate, String nameTag) {
		this.code = code;
		this.userIdx = userIdx;
		this.title = title;
		this.theme = theme;
		this.openDate = openDate;
		this.decos = toCsv(Deco.of(theme)); //기본값: 테마 장식 3개 전부
		this.nameTag = nameTag;
	}

	//개봉일이 지났는지
	public boolean isOpened() {
		return !LocalDateTime.now().isBefore(openDate);
	}

	//주인인지
	public boolean isOwner(Long loginIdx) {
		return loginIdx != null && userIdx.equals(loginIdx);
	}

	//CSV → Deco 목록 (모르는 코드는 건너뜀)
	public List<Deco> getDecoList() {
		if (decos == null || decos.isBlank()) {
			return List.of();
		}
		return Arrays.stream(decos.split(",")).map(Deco::fromCode).filter(Objects::nonNull).toList();
	}

	//화면 JS 용 코드 목록 ["KAGAMI", ...]
	public List<String> getDecoCodes() {
		return getDecoList().stream().map(Deco::name).toList();
	}

	//꾸미기: 이 테마의 장식만, 중복 없이. 슬롯 순 CSV 로 저장
	public void postDecorate(List<Deco> newDecos, String newNameTag) {
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

	//설정 수정. 기념일이 바뀌면 장식을 새 테마 기본값으로 초기화
	public void postModify(String title, Theme theme, LocalDateTime openDate) {
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
