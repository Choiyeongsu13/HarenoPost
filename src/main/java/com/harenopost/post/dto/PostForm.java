package com.harenopost.post.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.harenopost.post.Theme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** 포스트 만들기·설정 폼. お正月·クリスマス 는 날짜를 보내도 서버가 무시하고 직접 계산한다 */
@Getter
@Setter
public class PostForm {

	@NotNull(message = "記念日を選んでください")
	private Theme theme = Theme.SHOGATSU;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate openDate;

	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime openTime;

	@NotBlank(message = "ポスト名を入力してください")
	@Size(max = 20, message = "ポスト名は20文字までです")
	private String title;
}
