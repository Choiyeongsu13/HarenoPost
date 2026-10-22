package com.harenopost.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.harenopost.entity.PostEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//포스트 만들기·설정 입력값. お正月·クリスマス 는 날짜를 보내도 서버가 무시하고 직접 계산한다
@NoArgsConstructor
@Setter
@Getter
public class PostRequestDTO {

	@NotNull(message = "記念日を選んでください")
	private Theme theme = Theme.SHOGATSU;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate openDate;

	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime openTime;

	@NotBlank(message = "ポスト名を入力してください")
	@Size(max = 20, message = "ポスト名は20文字までです")
	private String title;

	//설정 폼에 현재 값 채우기
	public PostRequestDTO(PostResponseDTO post) {
		this.theme = post.getTheme();
		this.title = post.getTitle();
		this.openDate = post.getOpenDate().toLocalDate();
		this.openTime = post.getOpenDate().toLocalTime();
	}

	//DTO에서 필요한 부분을 entity화 (code, 주인, 개봉일, 이름표는 서비스에서 정한다)
	public PostEntity toEntity(String code, Long userIdx, LocalDateTime realOpenDate, String nameTag) {
		return PostEntity.builder()
				.code(code)
				.userIdx(userIdx)
				.title(title.strip())
				.theme(theme)
				.openDate(realOpenDate)
				.nameTag(nameTag)
				.build();
	}
}
