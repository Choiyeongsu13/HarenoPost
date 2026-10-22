package com.harenopost.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.harenopost.entity.UserEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

//회원 정보 (비밀번호는 담지 않는다)
@NoArgsConstructor
@Getter
public class UserResponseDTO {

	private Long userIdx;
	private String userid;
	private String nickname;
	private String email;
	private LocalDate birthday;
	private Role role;
	private LocalDateTime regdate;
	private LocalDateTime lastLogin;

	//entity -> dto
	public UserResponseDTO(UserEntity entity) {
		this.userIdx = entity.getUserIdx();
		this.userid = entity.getUserid();
		this.nickname = entity.getNickname();
		this.email = entity.getEmail();
		this.birthday = entity.getBirthday();
		this.role = entity.getRole();
		this.regdate = entity.getRegdate();
		this.lastLogin = entity.getLastLogin();
	}
}
