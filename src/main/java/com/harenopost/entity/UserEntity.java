package com.harenopost.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.harenopost.dto.Role;

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
@Table(name = "tbl_user")
@NoArgsConstructor
@Getter
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_GENERATOR")
	@SequenceGenerator(name = "seq_user_GENERATOR", sequenceName = "seq_user", allocationSize = 1)
	@Column(name = "user_idx")
	private Long userIdx;

	private String userid;
	private String passwd; //BCrypt
	private String nickname;
	private String email;
	private LocalDate birthday; //선택

	@Enumerated(EnumType.STRING)
	private Role role;

	private LocalDateTime regdate = LocalDateTime.now(); //가입일

	@Column(name = "last_login")
	private LocalDateTime lastLogin; //마지막 로그인

	@Builder
	public UserEntity(String userid, String passwd, String nickname, String email, LocalDate birthday) {
		this.userid = userid;
		this.passwd = passwd;
		this.nickname = nickname;
		this.email = email;
		this.birthday = birthday;
		this.role = Role.ROLE_USER;
	}

	//프로필 수정
	public void userModify(String nickname, String email, LocalDate birthday) {
		this.nickname = nickname;
		this.email = email;
		this.birthday = birthday;
	}

	//비밀번호 변경
	public void userPasswd(String passwd) {
		this.passwd = passwd;
	}

	//로그인 성공 시각
	public void userLoginTime() {
		this.lastLogin = LocalDateTime.now();
	}
}
