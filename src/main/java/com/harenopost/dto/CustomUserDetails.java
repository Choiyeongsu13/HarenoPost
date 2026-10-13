package com.harenopost.dto;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.harenopost.entity.User;

import lombok.Getter;

/** 로그인 회원 정보 (세션에 들어가므로 엔티티 대신 필요한 값만 둔다) */
@Getter
public class CustomUserDetails implements UserDetails {

	private final Long userIdx;
	private final String userid;
	private final String passwd;
	private final String role;

	public CustomUserDetails(User user) {
		this.userIdx = user.getUserIdx();
		this.userid = user.getUserid();
		this.passwd = user.getPasswd();
		this.role = user.getRole();
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
