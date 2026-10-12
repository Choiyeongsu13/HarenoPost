package com.harenopost.member.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupForm {

	@NotBlank(message = "IDを入力してください")
	@Pattern(regexp = "^[a-z][a-z0-9]{3,19}$", message = "IDは英小文字・数字 4〜20文字（先頭は英字）で入力してください")
	private String userid;

	@NotBlank(message = "パスワードを入力してください")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,20}$", message = "パスワードは英字と数字を含む 8〜20文字で入力してください")
	private String passwd;

	@NotBlank(message = "パスワード（確認）を入力してください")
	private String passwdConfirm;

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
