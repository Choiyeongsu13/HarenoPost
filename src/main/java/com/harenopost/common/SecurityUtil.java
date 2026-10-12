package com.harenopost.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.harenopost.member.CustomUserDetails;

/** 로그인 회원 정보 꺼내기 */
public final class SecurityUtil {

	private SecurityUtil() {
	}

	/** 로그인 회원 번호. 로그인하지 않았으면 null */
	public static Long currentMemberIdx() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
			return user.getMemberIdx();
		}
		return null;
	}

	/** 로그인 회원 번호. 로그인하지 않았으면 LOGIN_REQUIRED */
	public static Long requireMemberIdx() {
		Long idx = currentMemberIdx();
		if (idx == null) {
			throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
		}
		return idx;
	}
}
