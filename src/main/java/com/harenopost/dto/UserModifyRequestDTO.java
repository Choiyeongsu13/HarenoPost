package com.harenopost.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//프로필 수정 입력값 (닉네임, 메일, 생일)
@NoArgsConstructor
@Setter
@Getter
public class UserModifyRequestDTO {

	@NotBlank(message = "ニックネームを入力してください")
	@Size(max = 10, message = "ニックネームは1〜10文字で入力してください")
	private String nickname;

	@NotBlank(message = "メールアドレスを入力してください")
	@Email(message = "メールアドレスの形式を確認してください")
	@Size(max = 100, message = "メールアドレスが長すぎます")
	private String email;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthday;

	//수정 폼에 현재 값 채우기
	public UserModifyRequestDTO(UserResponseDTO user) {
		this.nickname = user.getNickname();
		this.email = user.getEmail();
		this.birthday = user.getBirthday();
	}
}
