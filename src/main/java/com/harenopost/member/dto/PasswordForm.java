package com.harenopost.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordForm {

	@NotBlank(message = "現在のパスワードを入力してください")
	private String currentPasswd;

	@NotBlank(message = "新しいパスワードを入力してください")
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,20}$", message = "パスワードは英字と数字を含む 8〜20文字で入力してください")
	private String newPasswd;

	@NotBlank(message = "新しいパスワード（確認）を入力してください")
	private String newPasswdConfirm;
}
