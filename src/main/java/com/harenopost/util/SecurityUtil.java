package com.harenopost.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;

// 로그인 회원 정보 꺼내기 
public final class SecurityUtil {

	private SecurityUtil() {
	}

	// 로그인 회원 번호. 로그인하지 않았으면 null
	public static Long currentUserIdx() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
			return user.getUserIdx();
		}
		return null;
	}

	// 로그인 회원 번호
	public static Long requireUserIdx() {
		Long idx = currentUserIdx();
		if (idx == null) {
			throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
		}
		return idx;
	}
}
