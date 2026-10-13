package com.harenopost.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {

	@NotBlank(message = "ニックネームを入力してください")
	@Size(max = 10, message = "ニックネームは1〜10文字で入力してください")
	private String nickname;

	@NotBlank(message = "メールアドレスを入力してください")
	@Email(message = "メールアドレスの形式を確認してください")
	@Size(max = 100, message = "メールアドレスが長すぎます")
	private String email;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthday;
}
