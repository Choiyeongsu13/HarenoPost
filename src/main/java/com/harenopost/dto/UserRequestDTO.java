package com.harenopost.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.harenopost.entity.UserEntity;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//회원가입 입력값
@NoArgsConstructor
@Setter
@Getter
public class UserRequestDTO {

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
	private LocalDate birthday; //선택

	//비밀번호와 확인이 같은지
	@AssertTrue(message = "パスワードが一致しません")
	public boolean isPasswdSame() {
		return passwd == null || passwd.equals(passwdConfirm);
	}

	//DTO에서 필요한 부분을 entity화 (passwd 는 암호화한 값을 넣는다)
	public UserEntity toEntity(String encodedPasswd) {
		return UserEntity.builder()
				.userid(userid)
				.passwd(encodedPasswd)
				.nickname(nickname.strip())
				.email(email)
				.birthday(birthday)
				.build();
	}
}
