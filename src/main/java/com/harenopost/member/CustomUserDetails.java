package com.harenopost.member;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

/** 로그인 회원 정보 (세션에 들어가므로 엔티티 대신 필요한 값만 둔다) */
@Getter
public class CustomUserDetails implements UserDetails {

	private final Long memberIdx;
	private final String userid;
	private final String passwd;
	private final String role;

	public CustomUserDetails(Member member) {
		this.memberIdx = member.getMemberIdx();
		this.userid = member.getUserid();
		this.passwd = member.getPasswd();
		this.role = member.getRole();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role));
	}

	@Override
	public String getPassword() {
		return passwd;
	}

	@Override
	public String getUsername() {
		return userid;
	}
}
