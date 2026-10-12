package com.harenopost.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	@Id
	@SequenceGenerator(name = "seq_member_gen", sequenceName = "seq_member", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_member_gen")
	@Column(name = "member_idx")
	private Long memberIdx;

	@Column(nullable = false, unique = true, length = 20)
	private String userid;

	@Column(nullable = false, length = 100)
	private String passwd;

	@Column(nullable = false, length = 30)
	private String nickname;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	private LocalDate birthday;

	@Column(nullable = false, length = 20)
	private String role;

	@Column(insertable = false, updatable = false)
	private LocalDateTime regdate;

	@Column(name = "last_login")
	private LocalDateTime lastLogin;

	public static Member create(String userid, String encodedPasswd, String nickname, String email, LocalDate birthday) {
		Member m = new Member();
		m.userid = userid;
		m.passwd = encodedPasswd;
		m.nickname = nickname;
		m.email = email;
		m.birthday = birthday;
		m.role = ROLE_USER;
		return m;
	}

	public void updateProfile(String nickname, String email, LocalDate birthday) {
		this.nickname = nickname;
		this.email = email;
		this.birthday = birthday;
	}

	public void changePassword(String encodedPasswd) {
		this.passwd = encodedPasswd;
	}

	public void loggedIn() {
		this.lastLogin = LocalDateTime.now();
	}
}
