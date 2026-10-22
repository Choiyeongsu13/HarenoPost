package com.harenopost.dto;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.harenopost.entity.UserEntity;

import lombok.Getter;

//로그인 회원 정보 (세션에 들어가므로 엔티티 대신 필요한 값만 둔다)
@Getter
public class CustomUserDetails implements UserDetails {

	private final Long userIdx;
	private final String userid;
	private final String passwd;
	private final Role role;

	public CustomUserDetails(UserEntity entity) {
		this.userIdx = entity.getUserIdx();
		this.userid = entity.getUserid();
		this.passwd = entity.getPasswd();
		this.role = entity.getRole();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.name()));
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
